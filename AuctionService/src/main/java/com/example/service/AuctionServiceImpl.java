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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public AuctionResponse createAuction(CreateAuctionRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new InvalidAuctionStateException("End time must be after start time");
        }

        Auction auction = Auction.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .sellerId(request.getSellerId())
                .startingPrice(request.getStartingPrice())
                .currentPrice(request.getStartingPrice())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(Auction.AuctionStatus.PENDING)
                .build();

        Auction saved = auctionRepository.save(auction);
        log.info("Auction created: id={}, title={}", saved.getId(), saved.getTitle());

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
    public AuctionResponse updateAuction(Long id, UpdateAuctionRequest request) {
        Auction auction = getAuctionEntity(id);
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
            auction.setStartingPrice(request.getStartingPrice());
        }
        if (request.getStartTime() != null) {
            auction.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            auction.setEndTime(request.getEndTime());
        }
        Auction updated = auctionRepository.save(auction);
        return AuctionResponse.fromEntity(updated);
    }

    @Override
    public AuctionResponse startAuction(Long id) {
        Auction auction = getAuctionEntity(id);
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
    public AuctionResponse closeAuction(Long id) {
        Auction auction = getAuctionEntity(id);
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
    public void deleteAuction(Long id) {
        if (!auctionRepository.existsById(id)) {
            throw new AuctionNotFoundException(id);
        }
        auctionRepository.deleteById(id);
    }

    @Override
    public Auction getAuctionEntity(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new AuctionNotFoundException(id));
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
        publish(TOPIC_CREATED, event);
    }

    private void publishStartedEvent(Auction auction) {
        AuctionStartedEvent event = AuctionStartedEvent.builder()
                .auctionId(auction.getId())
                .title(auction.getTitle())
                .sellerId(auction.getSellerId())
                .startedAt(LocalDateTime.now())
                .build();
        publish(TOPIC_STARTED, event);
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
        publish(TOPIC_CLOSED, event);
    }

    private void publish(String topic, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, payload);
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
            auction.setStatus(Auction.AuctionStatus.ACTIVE);
            auctionRepository.save(auction);
            publishStartedEvent(auction);
            log.info("Auction auto-started: id={}", auction.getId());
        }

        List<Auction> active = auctionRepository.findByStatusAndEndTimeBefore(Auction.AuctionStatus.ACTIVE, now);
        for (Auction auction : active) {
            auction.setStatus(Auction.AuctionStatus.CLOSED);
            auctionRepository.save(auction);
            publishClosedEvent(auction);
            log.info("Auction auto-closed: id={}", auction.getId());
        }

        List<Auction> expired = auctionRepository.findByStatusAndEndTimeBefore(Auction.AuctionStatus.PENDING, now);
        for (Auction auction : expired) {
            auction.setStatus(Auction.AuctionStatus.EXPIRED);
            auctionRepository.save(auction);
            log.info("Auction expired: id={}", auction.getId());
        }
    }
}
