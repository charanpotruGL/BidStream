package com.example.DTO;

import com.example.model.Bid;
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
public class BidResponse {

    private Long id;
    private Long auctionId;
    private Long bidderId;
    private BigDecimal amount;
    private Bid.BidStatus status;
    private LocalDateTime createdAt;

    public static BidResponse fromEntity(Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .auctionId(bid.getAuctionId())
                .bidderId(bid.getBidderId())
                .amount(bid.getAmount())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .build();
    }
}
