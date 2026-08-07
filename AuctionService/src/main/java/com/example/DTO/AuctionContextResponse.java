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
public class AuctionContextResponse {

    private Long id;
    private String title;
    private Long sellerId;
    private BigDecimal startingPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public static AuctionContextResponse fromEntity(Auction auction) {
        return AuctionContextResponse.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .sellerId(auction.getSellerId())
                .startingPrice(auction.getStartingPrice())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(auction.getStatus() != null ? auction.getStatus().name() : null)
                .build();
    }
}
