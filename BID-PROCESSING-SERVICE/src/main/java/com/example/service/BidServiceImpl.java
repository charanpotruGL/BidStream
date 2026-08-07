package com.example.service;

import com.example.DTO.BidResponse;
import com.example.DTO.CreateBidRequest;
import com.example.event.BidEvent;
import com.example.exception.BidNotFoundException;
import com.example.exception.InvalidBidException;
import com.example.model.Bid;
import com.example.repository.BidRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AuctionTitleCache auctionTitleCache;
    private final AuctionContextCache auctionContextCache;
    private final AuctionClient auctionClient;

    private static final String TOPIC_BID_PLACED = "bid-placed";
    private static final String TOPIC_BID_OUTBID = "bid-outbid";

    @Override
    @Transactional
    public BidResponse placeBid(CreateBidRequest request) {
        validateBid(request);
        bidRepository.lockAuctionById(request.getAuctionId());

        AuctionContextCache.AuctionContext context = resolveAuctionContext(request.getAuctionId());
        if (context == null) {
            throw new InvalidBidException("Auction not found or not accepting bids");
        }
        if (!"ACTIVE".equals(context.getStatus())) {
            throw new InvalidBidException("Auction is not accepting bids");
        }
        if (context.getEndTime() != null && !LocalDateTime.now().isBefore(context.getEndTime())) {
            throw new InvalidBidException("Auction has already ended");
        }
        if (context.getSellerId() != null && context.getSellerId().equals(request.getBidderId())) {
            throw new InvalidBidException("You cannot bid on your own auction");
        }

        Bid highest = bidRepository
                .findFirstByAuctionIdAndStatusOrderByAmountDesc(request.getAuctionId(), Bid.BidStatus.PLACED)
                .orElse(null);

        BigDecimal minimum = context.getStartingPrice() != null ? context.getStartingPrice() : BigDecimal.ZERO;
        if (highest != null && highest.getAmount().compareTo(minimum) > 0) {
            minimum = highest.getAmount();
        }
        if (request.getAmount().compareTo(minimum) <= 0) {
            throw new InvalidBidException("Bid amount must be higher than the current price");
        }

        if (highest != null) {
            highest.setStatus(Bid.BidStatus.OUTBID);
            bidRepository.save(highest);
            publishBidEvent(TOPIC_BID_OUTBID, highest);
        }

        Bid bid = Bid.builder()
                .auctionId(request.getAuctionId())
                .bidderId(request.getBidderId())
                .amount(request.getAmount())
                .status(Bid.BidStatus.PLACED)
                .build();

        Bid saved = bidRepository.save(bid);
        publishBidEvent(TOPIC_BID_PLACED, saved);

        log.info("Bid placed: id={}, auctionId={}, bidderId={}, amount={}",
                saved.getId(), saved.getAuctionId(), saved.getBidderId(), saved.getAmount());

        return BidResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void markAuctionOutcome(Long auctionId, Long winningBidId) {
        List<Bid> bids = bidRepository.findByAuctionIdOrderByIdAsc(auctionId);
        boolean changed = false;
        for (Bid bid : bids) {
            if (bid.getStatus() == Bid.BidStatus.PLACED) {
                bid.setStatus(bid.getId().equals(winningBidId) ? Bid.BidStatus.WINNING : Bid.BidStatus.LOST);
                changed = true;
            }
        }
        if (changed) {
            bidRepository.saveAll(bids);
            log.info("Marked outcome for auction {}: winningBidId={}", auctionId, winningBidId);
        }
    }

    @Override
    public BidResponse getBidById(Long bidId) {
        return BidResponse.fromEntity(getBidEntity(bidId));
    }

    @Override
    public List<BidResponse> getBidsByAuction(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByIdAsc(auctionId).stream()
                .map(BidResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BidResponse> getBidsByBidder(Long bidderId) {
        return bidRepository.findByBidderIdOrderByIdDesc(bidderId).stream()
                .map(BidResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public BidResponse getHighestBidForAuction(Long auctionId) {
        Bid highest = bidRepository.findFirstByAuctionIdOrderByAmountDesc(auctionId)
                .orElseThrow(() -> new BidNotFoundException("No bids found for auction: " + auctionId));
        return BidResponse.fromEntity(highest);
    }

    @Override
    public Bid getBidEntity(Long bidId) {
        return bidRepository.findById(bidId)
                .orElseThrow(() -> new BidNotFoundException(bidId));
    }

    private void validateBid(CreateBidRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBidException("Bid amount must be greater than zero");
        }
        if (request.getAuctionId() == null || request.getBidderId() == null) {
            throw new InvalidBidException("Auction ID and Bidder ID are required");
        }
    }

    private AuctionContextCache.AuctionContext resolveAuctionContext(Long auctionId) {
        AuctionContextCache.AuctionContext context = auctionContextCache.get(auctionId);
        if (context != null) {
            return context;
        }
        AuctionContextCache.AuctionContext fetched = auctionClient.fetchContext(auctionId);
        if (fetched == null) {
            return null;
        }
        auctionContextCache.put(auctionId, fetched);
        if (fetched.getTitle() != null) {
            auctionTitleCache.put(auctionId, fetched.getTitle());
        }
        return fetched;
    }

    private void publishBidEvent(String topic, Bid bid) {
        BidEvent event = BidEvent.builder()
                .bidId(bid.getId())
                .auctionId(bid.getAuctionId())
                .auctionTitle(auctionTitleCache.get(bid.getAuctionId()))
                .bidderId(bid.getBidderId())
                .amount(bid.getAmount())
                .timestamp(LocalDateTime.now())
                .build();
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, String.valueOf(bid.getAuctionId()), payload);
            log.info("Published event to topic {}: {}", topic, payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize bid event for topic {}", topic, e);
        }
    }
}
