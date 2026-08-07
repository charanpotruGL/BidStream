package com.example.listener;

import com.example.event.BidEvent;
import com.example.service.AuctionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BidEventListener {

    private final AuctionService auctionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "bid-placed", groupId = "${spring.kafka.consumer.group-id}")
    public void onBidPlaced(String message) {
        try {
            BidEvent event = objectMapper.readValue(message, BidEvent.class);
            log.info("Bid placed event received: bidId={}, auctionId={}, bidderId={}, amount={}",
                    event.getBidId(), event.getAuctionId(), event.getBidderId(), event.getAmount());
            auctionService.recordBid(event.getAuctionId(), event.getBidId(), event.getBidderId(), event.getAmount());
        } catch (Exception e) {
            log.error("Failed to process bid-placed event: {}", message, e);
        }
    }
}
