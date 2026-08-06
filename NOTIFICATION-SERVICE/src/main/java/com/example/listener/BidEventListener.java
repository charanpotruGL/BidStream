package com.example.listener;

import com.example.event.BidEvent;
import com.example.model.Notification;
import com.example.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BidEventListener {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "bid-placed", groupId = "${spring.kafka.consumer.group-id}")
    public void onBidPlaced(String message) {
        try {
            BidEvent event = objectMapper.readValue(message, BidEvent.class);
            Notification notification = Notification.builder()
                    .userId(event.getBidderId())
                    .notificationType(Notification.NotificationType.BID_PLACED)
                    .title("Bid Placed")
                    .message("Your bid of $" + event.getAmount() + " was placed on auction " + event.getAuctionId() + ".")
                    .build();
            notificationRepository.save(notification);
            log.info("Notification created for bid-placed event: bidId={}", event.getBidId());
        } catch (Exception e) {
            log.error("Failed to process bid-placed event for notification: {}", message, e);
        }
    }

    @KafkaListener(topics = "bid-outbid", groupId = "${spring.kafka.consumer.group-id}")
    public void onBidOutbid(String message) {
        try {
            BidEvent event = objectMapper.readValue(message, BidEvent.class);
            Notification notification = Notification.builder()
                    .userId(event.getBidderId())
                    .notificationType(Notification.NotificationType.BID_OUTBID)
                    .title("You've Been Outbid")
                    .message("Your bid on auction " + event.getAuctionId() + " has been outbid.")
                    .build();
            notificationRepository.save(notification);
            log.info("Notification created for bid-outbid event: bidId={}", event.getBidId());
        } catch (Exception e) {
            log.error("Failed to process bid-outbid event for notification: {}", message, e);
        }
    }
}
