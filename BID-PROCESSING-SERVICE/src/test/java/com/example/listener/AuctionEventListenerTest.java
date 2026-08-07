package com.example.listener;

import com.example.event.AuctionEvent;
import com.example.service.AuctionContextCache;
import com.example.service.AuctionTitleCache;
import com.example.service.BidService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AuctionEventListenerTest {

    @Mock
    private BidService bidService;

    @Mock
    private ObjectMapper objectMapper;

    private final AuctionTitleCache auctionTitleCache = new AuctionTitleCache();
    private final AuctionContextCache auctionContextCache = new AuctionContextCache();

    private AuctionEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new AuctionEventListener(auctionTitleCache, auctionContextCache, bidService, objectMapper);
    }

    private AuctionEvent event() {
        return AuctionEvent.builder()
                .auctionId(1L)
                .title("Vintage Watch")
                .sellerId(3L)
                .startingPrice(new BigDecimal("100.00"))
                .winningBidId(9L)
                .winningBidderId(9L)
                .finalPrice(new BigDecimal("250.00"))
                .build();
    }

    @Test
    void onAuctionCreated_validMessage_storesContext() throws Exception {
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenReturn(event());

        listener.onAuctionCreated("{\"json\":true}");

        AuctionContextCache.AuctionContext context = auctionContextCache.get(1L);
        assertThat(context.getStatus()).isEqualTo("PENDING");
        assertThat(context.getStartingPrice()).isEqualByComparingTo("100.00");
        assertThat(context.getSellerId()).isEqualTo(3L);
        assertThat(auctionTitleCache.get(1L)).isEqualTo("Vintage Watch");
    }

    @Test
    void onAuctionCreated_invalidMessage_doesNotThrow() throws Exception {
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenThrow(new RuntimeException("bad json"));

        assertThatCode(() -> listener.onAuctionCreated("not-json")).doesNotThrowAnyException();
    }

    @Test
    void onAuctionStarted_validMessage_setsActive() throws Exception {
        auctionContextCache.put(1L, AuctionContextCache.AuctionContext.builder()
                .title("Vintage Watch").status("PENDING").build());
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenReturn(event());

        listener.onAuctionStarted("{\"json\":true}");

        assertThat(auctionContextCache.get(1L).getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void onAuctionClosed_validMessage_marksOutcome() throws Exception {
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenReturn(event());

        listener.onAuctionClosed("{\"json\":true}");

        assertThat(auctionContextCache.get(1L).getStatus()).isEqualTo("CLOSED");
        verify(bidService).markAuctionOutcome(1L, 9L);
    }

    @Test
    void onAuctionClosed_invalidMessage_doesNotThrow() throws Exception {
        when(objectMapper.readValue(anyString(), eq(AuctionEvent.class))).thenThrow(new RuntimeException("bad json"));

        assertThatCode(() -> listener.onAuctionClosed("not-json")).doesNotThrowAnyException();
    }
}
