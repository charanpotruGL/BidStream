package com.example.listener;

import com.example.event.BidEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidEventListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    private BidEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new BidEventListener(objectMapper);
    }

    private BidEvent event() {
        return BidEvent.builder()
                .bidId(1L)
                .auctionId(10L)
                .bidderId(100L)
                .amount(new BigDecimal("150.00"))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void onBidPlaced_validMessage_parsed() throws Exception {
        when(objectMapper.readValue(anyString(), eq(BidEvent.class))).thenReturn(event());

        assertThatCode(() -> listener.onBidPlaced("{\"json\":true}")).doesNotThrowAnyException();
    }

    @Test
    void onBidPlaced_invalidMessage_doesNotThrow() throws Exception {
        when(objectMapper.readValue(anyString(), eq(BidEvent.class))).thenThrow(new RuntimeException("bad json"));

        assertThatCode(() -> listener.onBidPlaced("not-json")).doesNotThrowAnyException();
    }

    @Test
    void onBidOutbid_validMessage_parsed() throws Exception {
        when(objectMapper.readValue(anyString(), eq(BidEvent.class))).thenReturn(event());

        assertThatCode(() -> listener.onBidOutbid("{\"json\":true}")).doesNotThrowAnyException();
    }

    @Test
    void onBidOutbid_invalidMessage_doesNotThrow() throws Exception {
        when(objectMapper.readValue(anyString(), eq(BidEvent.class))).thenThrow(new RuntimeException("bad json"));

        assertThatCode(() -> listener.onBidOutbid("not-json")).doesNotThrowAnyException();
    }
}
