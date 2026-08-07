package com.example.listener;

import com.example.event.AuctionEvent;
import com.example.service.AuctionContextCache;
import com.example.service.AuctionTitleCache;
import com.example.service.BidService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionEventListener {

    private final AuctionTitleCache auctionTitleCache;
    private final AuctionContextCache auctionContextCache;
    private final BidService bidService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "auction-created", groupId = "${spring.kafka.consumer.group-id}")
    public void onAuctionCreated(String message) {
        try {
            AuctionEvent event = objectMapper.readValue(message, AuctionEvent.class);
            auctionTitleCache.put(event.getAuctionId(), event.getTitle());
            auctionContextCache.put(event.getAuctionId(), AuctionContextCache.AuctionContext.builder()
                    .title(event.getTitle())
                    .sellerId(event.getSellerId())
                    .startingPrice(event.getStartingPrice())
                    .startTime(event.getStartTime())
                    .endTime(event.getEndTime())
                    .status("PENDING")
                    .build());
            log.info("Auction created event received: auctionId={}, title={}", event.getAuctionId(), event.getTitle());
        } catch (Exception e) {
            log.error("Failed to process auction-created event: {}", message, e);
        }
    }

    @KafkaListener(topics = "auction-started", groupId = "${spring.kafka.consumer.group-id}")
    public void onAuctionStarted(String message) {
        try {
            AuctionEvent event = objectMapper.readValue(message, AuctionEvent.class);
            auctionTitleCache.put(event.getAuctionId(), event.getTitle());
            AuctionContextCache.AuctionContext context = auctionContextCache.get(event.getAuctionId());
            if (context == null) {
                context = AuctionContextCache.AuctionContext.builder().title(event.getTitle()).build();
            }
            context.setStatus("ACTIVE");
            auctionContextCache.put(event.getAuctionId(), context);
            log.info("Auction started event received: auctionId={}", event.getAuctionId());
        } catch (Exception e) {
            log.error("Failed to process auction-started event: {}", message, e);
        }
    }

    @KafkaListener(topics = "auction-closed", groupId = "${spring.kafka.consumer.group-id}")
    public void onAuctionClosed(String message) {
        try {
            AuctionEvent event = objectMapper.readValue(message, AuctionEvent.class);
            auctionTitleCache.put(event.getAuctionId(), event.getTitle());
            AuctionContextCache.AuctionContext context = auctionContextCache.get(event.getAuctionId());
            if (context == null) {
                context = AuctionContextCache.AuctionContext.builder().title(event.getTitle()).build();
            }
            context.setStatus("CLOSED");
            auctionContextCache.put(event.getAuctionId(), context);
            bidService.markAuctionOutcome(event.getAuctionId(), event.getWinningBidId());
            log.info("Auction closed event received: auctionId={}, winner={}", event.getAuctionId(), event.getWinningBidderId());
        } catch (Exception e) {
            log.error("Failed to process auction-closed event: {}", message, e);
        }
    }
}
