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

    private static final String TOPIC_BID_PLACED = "bid-placed";
    private static final String TOPIC_BID_OUTBID = "bid-outbid";

    @Override
    public BidResponse placeBid(CreateBidRequest request) {
        validateBid(request);

        Bid highest = bidRepository
                .findFirstByAuctionIdAndStatusOrderByAmountDesc(request.getAuctionId(), Bid.BidStatus.PLACED)
                .orElse(null);

        if (highest != null && highest.getAmount().compareTo(request.getAmount()) >= 0) {
            throw new InvalidBidException("Bid amount must be higher than the current highest bid");
        }

        // Mark existing highest as outbid
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
    public BidResponse getBidById(Long bidId) {
        return BidResponse.fromEntity(getBidEntity(bidId));
    }

    @Override
    public List<BidResponse> getBidsByAuction(Long auctionId) {
        return bidRepository.findByAuctionId(auctionId).stream()
                .map(BidResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BidResponse> getBidsByBidder(Long bidderId) {
        return bidRepository.findByBidderId(bidderId).stream()
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

    private void publishBidEvent(String topic, Bid bid) {
        BidEvent event = BidEvent.builder()
                .bidId(bid.getId())
                .auctionId(bid.getAuctionId())
                .bidderId(bid.getBidderId())
                .amount(bid.getAmount())
                .timestamp(LocalDateTime.now())
                .build();
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, payload);
            log.info("Published event to topic {}: {}", topic, payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize bid event for topic {}", topic, e);
        }
    }
}
