package com.example.service;

import com.example.DTO.AuctionResponse;
import com.example.DTO.CreateAuctionRequest;
import com.example.DTO.UpdateAuctionRequest;
import com.example.event.AuctionClosedEvent;
import com.example.event.AuctionCreatedEvent;
import com.example.event.AuctionStartedEvent;
import com.example.exception.AuctionNotFoundException;
import com.example.exception.InvalidAuctionStateException;
import com.example.model.Auction;
import com.example.repository.AuctionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_CREATED = "auction-created";
    private static final String TOPIC_STARTED = "auction-started";
    private static final String TOPIC_CLOSED = "auction-closed";
    private static final int MAX_AUCTION_AGE_DAYS = 5;

    @Override
    @Transactional
    public AuctionResponse createAuction(CreateAuctionRequest request, Long sellerId) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new InvalidAuctionStateException("End time must be after start time");
        }

        Auction auction = Auction.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .sellerId(sellerId)
                .startingPrice(request.getStartingPrice())
                .currentPrice(request.getStartingPrice())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(Auction.AuctionStatus.PENDING)
                .build();

        Auction saved = auctionRepository.save(auction);
        log.info("Auction created: id={}, title={}, sellerId={}", saved.getId(), saved.getTitle(), saved.getSellerId());

        publishCreatedEvent(saved);
        return AuctionResponse.fromEntity(saved);
    }

    @Override
    public AuctionResponse getAuctionById(Long id) {
        return AuctionResponse.fromEntity(getAuctionEntity(id));
    }

    @Override
    public Page<AuctionResponse> getAllAuctions(Pageable pageable) {
        return auctionRepository.findAll(pageable).map(AuctionResponse::fromEntity);
    }

    @Override
    public Page<AuctionResponse> getAuctionsByStatus(Auction.AuctionStatus status, Pageable pageable) {
        return auctionRepository.findByStatus(status, pageable).map(AuctionResponse::fromEntity);
    }

    @Override
    public List<AuctionResponse> getAuctionsBySeller(Long sellerId) {
        return auctionRepository.findBySellerId(sellerId).stream()
                .map(AuctionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long getActiveAuctionCount() {
        return auctionRepository.countByStatus(Auction.AuctionStatus.ACTIVE);
    }

    @Override
    @Transactional
    public AuctionResponse updateAuction(Long id, UpdateAuctionRequest request, Long sellerId) {
        Auction auction = getAuctionEntity(id);
        assertSellerOwns(auction, sellerId);
        if (auction.getStatus() != Auction.AuctionStatus.PENDING) {
            throw new InvalidAuctionStateException("Only PENDING auctions can be updated");
        }
        if (request.getTitle() != null) {
            auction.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            auction.setDescription(request.getDescription());
        }
        if (request.getStartingPrice() != null) {
            if (auction.getHighestBidId() == null) {
                auction.setCurrentPrice(request.getStartingPrice());
            }
            auction.setStartingPrice(request.getStartingPrice());
        }
        if (request.getStartTime() != null) {
            auction.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            auction.setEndTime(request.getEndTime());
        }
        validateTimes(auction.getStartTime(), auction.getEndTime());
        Auction updated = auctionRepository.save(auction);
        return AuctionResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public AuctionResponse startAuction(Long id, Long sellerId) {
        Auction auction = getAuctionEntity(id);
        assertSellerOwns(auction, sellerId);
        if (auction.getStatus() != Auction.AuctionStatus.PENDING) {
            throw new InvalidAuctionStateException("Only PENDING auctions can be started");
        }
        auction.setStatus(Auction.AuctionStatus.ACTIVE);
        Auction started = auctionRepository.save(auction);
        log.info("Auction started: id={}", started.getId());

        publishStartedEvent(started);
        return AuctionResponse.fromEntity(started);
    }

    @Override
    @Transactional
    public AuctionResponse closeAuction(Long id, Long sellerId) {
        Auction auction = getAuctionEntity(id);
        assertSellerOwns(auction, sellerId);
        if (auction.getStatus() != Auction.AuctionStatus.ACTIVE) {
            throw new InvalidAuctionStateException("Only ACTIVE auctions can be closed");
        }
        auction.setStatus(Auction.AuctionStatus.CLOSED);
        Auction closed = auctionRepository.save(auction);
        log.info("Auction closed: id={}", closed.getId());

        publishClosedEvent(closed);
        return AuctionResponse.fromEntity(closed);
    }

    @Override
    @Transactional
    public void deleteAuction(Long id, Long sellerId) {
        Auction auction = getAuctionEntity(id);
        assertSellerOwns(auction, sellerId);
        auctionRepository.delete(auction);
    }

    @Override
    public Auction getAuctionEntity(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new AuctionNotFoundException(id));
    }

    @Override
    @Transactional
    public void recordBid(Long auctionId, Long bidId, Long bidderId, BigDecimal amount) {
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null || auction.getStatus() != Auction.AuctionStatus.ACTIVE) {
            return;
        }
        if (auction.getCurrentPrice() != null && amount.compareTo(auction.getCurrentPrice()) <= 0) {
            return;
        }
        auction.setCurrentPrice(amount);
        auction.setHighestBidId(bidId);
        auction.setHighestBidderId(bidderId);
        auctionRepository.save(auction);
        log.info("Auction {} highest bid updated: bidId={}, bidderId={}, amount={}",
                auctionId, bidId, bidderId, amount);
    }

    private void assertSellerOwns(Auction auction, Long sellerId) {
        if (sellerId == null || !auction.getSellerId().equals(sellerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to perform this action");
        }
    }

    private void validateTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidAuctionStateException("End time must be after start time");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAuctionStateException("Start time must be in the future");
        }
        if (endTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAuctionStateException("End time must be in the future");
        }
    }

    private void publishCreatedEvent(Auction auction) {
        AuctionCreatedEvent event = AuctionCreatedEvent.builder()
                .auctionId(auction.getId())
                .title(auction.getTitle())
                .sellerId(auction.getSellerId())
                .startingPrice(auction.getStartingPrice())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .build();
        publish(TOPIC_CREATED, auction.getId(), event);
    }

    private void publishStartedEvent(Auction auction) {
        AuctionStartedEvent event = AuctionStartedEvent.builder()
                .auctionId(auction.getId())
                .title(auction.getTitle())
                .sellerId(auction.getSellerId())
                .startedAt(LocalDateTime.now())
                .build();
        publish(TOPIC_STARTED, auction.getId(), event);
    }

    private void publishClosedEvent(Auction auction) {
        AuctionClosedEvent event = AuctionClosedEvent.builder()
                .auctionId(auction.getId())
                .title(auction.getTitle())
                .sellerId(auction.getSellerId())
                .winningBidId(auction.getHighestBidId())
                .winningBidderId(auction.getHighestBidderId())
                .finalPrice(auction.getCurrentPrice())
                .closedAt(LocalDateTime.now())
                .build();
        publish(TOPIC_CLOSED, auction.getId(), event);
    }

    private void publish(String topic, Long key, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, String.valueOf(key), payload);
            log.info("Published event to topic {}: {}", topic, payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for topic {}", topic, e);
        }
    }

    @Scheduled(fixedRateString = "${auction.scheduler.interval-ms:60000}", initialDelayString = "${auction.scheduler.initial-delay-ms:30000}")
    @Transactional
    public void processAuctionStateTransitions() {
        LocalDateTime now = LocalDateTime.now();

        List<Auction> pending = auctionRepository.findByStatusAndStartTimeBefore(Auction.AuctionStatus.PENDING, now);
        for (Auction auction : pending) {
            Auction current = auctionRepository.findById(auction.getId()).orElse(null);
            if (current == null || current.getStatus() != Auction.AuctionStatus.PENDING) {
                continue;
            }
            current.setStatus(Auction.AuctionStatus.ACTIVE);
            auctionRepository.save(current);
            publishStartedEvent(current);
            log.info("Auction auto-started: id={}", current.getId());
        }

        List<Auction> active = auctionRepository.findByStatusAndEndTimeBefore(Auction.AuctionStatus.ACTIVE, now);
        for (Auction auction : active) {
            Auction current = auctionRepository.findById(auction.getId()).orElse(null);
            if (current == null || current.getStatus() != Auction.AuctionStatus.ACTIVE) {
                continue;
            }
            current.setStatus(Auction.AuctionStatus.CLOSED);
            auctionRepository.save(current);
            publishClosedEvent(current);
            log.info("Auction auto-closed: id={}", current.getId());
        }

        List<Auction> expired = auctionRepository
                .findByStatusAndEndTimeBeforeAndStartTimeAfter(Auction.AuctionStatus.PENDING, now, now);
        for (Auction auction : expired) {
            Auction current = auctionRepository.findById(auction.getId()).orElse(null);
            if (current == null || current.getStatus() != Auction.AuctionStatus.PENDING) {
                continue;
            }
            current.setStatus(Auction.AuctionStatus.EXPIRED);
            auctionRepository.save(current);
            log.info("Auction expired: id={}", current.getId());
        }
    }

    @Scheduled(cron = "${auction.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void deleteOldAuctions() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(MAX_AUCTION_AGE_DAYS);
        List<Auction> oldAuctions = auctionRepository.findByStatusInAndEndTimeBefore(
                List.of(Auction.AuctionStatus.CLOSED, Auction.AuctionStatus.EXPIRED), cutoff);
        if (oldAuctions.isEmpty()) {
            return;
        }
        oldAuctions.forEach(auction -> auctionRepository.delete(auction));
        log.info("Deleted {} auctions older than {} days", oldAuctions.size(), MAX_AUCTION_AGE_DAYS);
    }
}
