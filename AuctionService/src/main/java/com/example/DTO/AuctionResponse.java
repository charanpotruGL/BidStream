package com.example.DTO;

import com.example.model.Auction;
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
public class AuctionResponse {

    private Long id;
    private String title;
    private String description;
    private Long sellerId;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private Long highestBidId;
    private Long highestBidderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Auction.AuctionStatus status;
    private LocalDateTime createdAt;

    public static AuctionResponse fromEntity(Auction auction) {
        return AuctionResponse.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .description(auction.getDescription())
                .sellerId(auction.getSellerId())
                .startingPrice(auction.getStartingPrice())
                .currentPrice(auction.getCurrentPrice())
                .highestBidId(auction.getHighestBidId())
                .highestBidderId(auction.getHighestBidderId())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(auction.getStatus())
                .createdAt(auction.getCreatedAt())
                .build();
    }
}
