package com.example.listener;

import com.example.event.AuctionEvent;
import com.example.model.Notification;
import com.example.repository.NotificationRepository;
import com.example.service.EmailService;
import com.example.service.UserEmailResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private ObjectMapper objectMapper;

    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationEventListener(
                notificationRepository, emailService, new UserEmailResolver("user{userId}@example.com"), objectMapper);
    }

    private AuctionEvent event() {
        return AuctionEvent.builder()
                .auctionId(1L)
                .title("Vintage Watch")
                .sellerId(3L)
                .finalPrice(new BigDecimal("250.00"))
                .build();
    }

    private AuctionEvent wonEvent() {
        return AuctionEvent.builder()
                .auctionId(1L)
                .title("Vintage Watch")
                .sellerId(3L)
                .winningBidderId(9L)
                .finalPrice(new BigDecimal("250.00"))
                .build();
    }

    @Test
    void onAuctionCreated_savesNotificationAndSendsEmail() throws Exception {
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenReturn(event());

        listener.onAuctionCreated("json");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(3L);
        assertThat(saved.getNotificationType()).isEqualTo(Notification.NotificationType.AUCTION_CREATED);
        assertThat(saved.getTitle()).isEqualTo("Auction Created");
        assertThat(saved.getMessage()).contains("Vintage Watch");
        verify(emailService).sendEmail(eq("user3@example.com"), eq("Auction Created"), anyString());
    }

    @Test
    void onAuctionStarted_savesNotificationForSeller() throws Exception {
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenReturn(event());

        listener.onAuctionStarted("json");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getNotificationType())
                .isEqualTo(Notification.NotificationType.AUCTION_STARTED);
    }

    @Test
    void onAuctionClosed_savesNotificationForSeller() throws Exception {
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenReturn(event());

        listener.onAuctionClosed("json");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getNotificationType())
                .isEqualTo(Notification.NotificationType.AUCTION_CLOSED);
    }

    @Test
    void onAuctionClosed_withWinner_notifiesSellerAndWinner() throws Exception {
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenReturn(wonEvent());

        listener.onAuctionClosed("json");

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendEmail(eq("user3@example.com"), eq("Auction Closed"), anyString());
        verify(emailService).sendEmail(eq("user9@example.com"), eq("You Won the Auction!"), anyString());
    }

    @Test
    void onAuctionCreated_duplicateEvent_skipsSave() throws Exception {
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenReturn(event());
        when(notificationRepository.existsByUserIdAndNotificationTypeAndTitleAndMessage(
                eq(3L), eq(Notification.NotificationType.AUCTION_CREATED), anyString(), anyString()))
                .thenReturn(true);

        listener.onAuctionCreated("json");

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void onAuctionCreated_invalidMessage_doesNotThrow() throws Exception {
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenThrow(new RuntimeException("bad json"));

        assertThatCode(() -> listener.onAuctionCreated("not-json")).doesNotThrowAnyException();
    }
}
