package com.example.listener;

import com.example.event.BidEvent;
import com.example.model.Notification;
import com.example.repository.NotificationRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidEventListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ObjectMapper objectMapper;

    private BidEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new BidEventListener(notificationRepository, objectMapper);
    }

    private BidEvent event() {
        return BidEvent.builder()
                .bidId(1L)
                .auctionId(10L)
                .bidderId(100L)
                .amount(new BigDecimal("150.00"))
                .build();
    }

    @Test
    void onBidPlaced_savesNotificationForBidder() throws Exception {
        when(objectMapper.readValue(anyString(), eq(BidEvent.class))).thenReturn(event());

        listener.onBidPlaced("json");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(100L);
        assertThat(saved.getNotificationType()).isEqualTo(Notification.NotificationType.BID_PLACED);
        assertThat(saved.getTitle()).isEqualTo("Bid Placed");
        assertThat(saved.getMessage()).contains("150.00");
    }

    @Test
    void onBidOutbid_savesNotificationForBidder() throws Exception {
        when(objectMapper.readValue(anyString(), eq(BidEvent.class))).thenReturn(event());

        listener.onBidOutbid("json");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getNotificationType())
                .isEqualTo(Notification.NotificationType.BID_OUTBID);
        assertThat(captor.getValue().getTitle()).isEqualTo("You've Been Outbid");
    }

    @Test
    void onBidOutbid_usesAuctionNameInMessage() throws Exception {
        BidEvent bidEvent = event();
        bidEvent.setAuctionTitle("Antique Vase");
        when(objectMapper.readValue(anyString(), eq(BidEvent.class))).thenReturn(bidEvent);

        listener.onBidOutbid("json");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getMessage()).contains("Antique Vase");
        assertThat(captor.getValue().getMessage()).doesNotContain("auction 10");
    }

    @Test
    void onBidPlaced_usesAuctionNameInMessage() throws Exception {
        BidEvent bidEvent = event();
        bidEvent.setAuctionTitle("Antique Vase");
        when(objectMapper.readValue(anyString(), eq(BidEvent.class))).thenReturn(bidEvent);

        listener.onBidPlaced("json");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getMessage()).contains("Antique Vase");
        assertThat(captor.getValue().getMessage()).doesNotContain("auction 10");
    }

    @Test
    void onBidPlaced_invalidMessage_doesNotThrow() throws Exception {
        when(objectMapper.readValue(anyString(), eq(BidEvent.class))).thenThrow(new RuntimeException("bad json"));

        assertThatCode(() -> listener.onBidPlaced("not-json")).doesNotThrowAnyException();
    }

    @Test
    void onBidOutbid_invalidMessage_doesNotThrow() throws Exception {
        when(objectMapper.readValue(anyString(), eq(BidEvent.class))).thenThrow(new RuntimeException("bad json"));

        assertThatCode(() -> listener.onBidOutbid("not-json")).doesNotThrowAnyException();
    }
}
