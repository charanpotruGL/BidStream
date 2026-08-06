package com.example.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionStartedEvent {

    private Long auctionId;
    private String title;
    private Long sellerId;
    private LocalDateTime startedAt;
    private String eventType = "AUCTION_STARTED";
}
