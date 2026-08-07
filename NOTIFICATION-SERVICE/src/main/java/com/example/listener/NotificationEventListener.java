package com.example.listener;

import com.example.event.AuctionEvent;
import com.example.model.Notification;
import com.example.repository.NotificationRepository;
import com.example.service.EmailService;
import com.example.service.UserEmailResolver;
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
    private final EmailService emailService;
    private final UserEmailResolver userEmailResolver;
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
            saveIfAbsent(notification, "auction-created", event.getAuctionId());
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
            saveIfAbsent(notification, "auction-started", event.getAuctionId());
        } catch (Exception e) {
            log.error("Failed to process auction-started event for notification: {}", message, e);
        }
    }

    @KafkaListener(topics = "auction-closed", groupId = "${spring.kafka.consumer.group-id}")
    public void onAuctionClosed(String message) {
        try {
            AuctionEvent event = objectMapper.readValue(message, AuctionEvent.class);
            String winner = event.getWinningBidderId() != null
                    ? " Won by bidder #" + event.getWinningBidderId()
                            + (event.getFinalPrice() != null ? " for " + event.getFinalPrice() + "." : ".")
                    : " No winning bid was placed.";
            Notification notification = Notification.builder()
                    .userId(event.getSellerId())
                    .notificationType(Notification.NotificationType.AUCTION_CLOSED)
                    .title("Auction Closed")
                    .message("Your auction '" + event.getTitle() + "' has closed." + winner)
                    .build();
            saveIfAbsent(notification, "auction-closed", event.getAuctionId());
            if (event.getWinningBidderId() != null) {
                Notification winnerNotification = Notification.builder()
                        .userId(event.getWinningBidderId())
                        .notificationType(Notification.NotificationType.AUCTION_CLOSED)
                        .title("You Won the Auction!")
                        .message("Congratulations! You won the auction '" + event.getTitle()
                                + (event.getFinalPrice() != null ? "' for " + event.getFinalPrice() + "." : "'."))
                        .build();
                saveIfAbsent(winnerNotification, "auction-closed", event.getAuctionId());
            }
        } catch (Exception e) {
            log.error("Failed to process auction-closed event for notification: {}", message, e);
        }
    }

    private void saveIfAbsent(Notification notification, String eventName, Long auctionId) {
        boolean exists = notificationRepository.existsByUserIdAndNotificationTypeAndTitleAndMessage(
                notification.getUserId(), notification.getNotificationType(),
                notification.getTitle(), notification.getMessage());
        if (exists) {
            log.info("Skipping duplicate notification for event {} auctionId={}", eventName, auctionId);
            return;
        }
        notificationRepository.save(notification);
        emailService.sendEmail(userEmailResolver.resolve(notification.getUserId()),
                notification.getTitle(), notification.getMessage());
        log.info("Notification created for event {}: auctionId={}, userId={}", eventName, auctionId, notification.getUserId());
    }
}
