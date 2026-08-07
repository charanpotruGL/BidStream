package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auctions1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {

    public enum AuctionStatus {
        PENDING, ACTIVE, EXPIRED, CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "starting_price", nullable = false, precision = 15, scale = 2)
    @Digits(integer = 13, fraction = 2, message = "Starting price must have at most 2 decimal places")
    private BigDecimal startingPrice;

    @Column(name = "current_price", precision = 15, scale = 2)
    @Digits(integer = 13, fraction = 2, message = "Current price must have at most 2 decimal places")
    private BigDecimal currentPrice;

    @Column(name = "highest_bid_id")
    private Long highestBidId;

    @Column(name = "highest_bidder_id")
    private Long highestBidderId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuctionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AuctionStatus.PENDING;
        }
        if (this.currentPrice == null) {
            this.currentPrice = this.startingPrice;
        }
    }
}
