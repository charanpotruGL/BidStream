package com.example.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAuctionRequest {

    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Starting price must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Starting price must have at most 2 decimal places")
    private BigDecimal startingPrice;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
