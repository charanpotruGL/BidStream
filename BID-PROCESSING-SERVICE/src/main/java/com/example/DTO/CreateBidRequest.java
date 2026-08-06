package com.example.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBidRequest {

    @NotNull(message = "Auction ID is required")
    private Long auctionId;

    @NotNull(message = "Bidder ID is required")
    private Long bidderId;

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    private BigDecimal amount;
}
