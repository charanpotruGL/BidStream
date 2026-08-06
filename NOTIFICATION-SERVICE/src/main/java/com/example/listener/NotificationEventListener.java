package com.example.listener;

import com.example.event.AuctionEvent;
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
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "auction-created", groupId = "${spring.kafka.consumer.group-id}")
    public void onAuctionCreated(String message) {
        try {
            AuctionEvent event = objectMapper.readValue(message, AuctionEvent.class);
            Notification notification = Notification.builder()
                    .userId(event.getSellerId())
                    .notificationType(Notification.NotificationType.AUCTION_CREATED)
                    .title("Auction Created")
                    .message("Your auction '" + event.getTitle() + "' has been created successfully.")
                    .build();
            notificationRepository.save(notification);
            log.info("Notification created for auction-created event: auctionId={}", event.getAuctionId());
        } catch (Exception e) {
            log.error("Failed to process auction-created event for notification: {}", message, e);
        }
    }

    @KafkaListener(topics = "auction-started", groupId = "${spring.kafka.consumer.group-id}")
    public void onAuctionStarted(String message) {
        try {
            AuctionEvent event = objectMapper.readValue(message, AuctionEvent.class);
            Notification notification = Notification.builder()
                    .userId(event.getSellerId())
                    .notificationType(Notification.NotificationType.AUCTION_STARTED)
                    .title("Auction Started")
                    .message("Your auction '" + event.getTitle() + "' has started.")
                    .build();
            notificationRepository.save(notification);
            log.info("Notification created for auction-started event: auctionId={}", event.getAuctionId());
        } catch (Exception e) {
            log.error("Failed to process auction-started event for notification: {}", message, e);
        }
    }

    @KafkaListener(topics = "auction-closed", groupId = "${spring.kafka.consumer.group-id}")
    public void onAuctionClosed(String message) {
        try {
            AuctionEvent event = objectMapper.readValue(message, AuctionEvent.class);
            Notification notification = Notification.builder()
                    .userId(event.getSellerId())
                    .notificationType(Notification.NotificationType.AUCTION_CLOSED)
                    .title("Auction Closed")
                    .message("Your auction '" + event.getTitle() + "' has closed.")
                    .build();
            notificationRepository.save(notification);
            log.info("Notification created for auction-closed event: auctionId={}", event.getAuctionId());
        } catch (Exception e) {
            log.error("Failed to process auction-closed event for notification: {}", message, e);
        }
    }
}
