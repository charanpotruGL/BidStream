package com.example.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionClosedEvent {

    private Long auctionId;
    private String title;
    private Long sellerId;
    private Long winningBidId;
    private Long winningBidderId;
    private BigDecimal finalPrice;
    private LocalDateTime closedAt;
    private String eventType = "AUCTION_CLOSED";
}
