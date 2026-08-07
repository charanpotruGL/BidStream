package com.example.service;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuctionContextCache {

    @Data
    @Builder
    public static class AuctionContext {
        private String title;
        private Long sellerId;
        private BigDecimal startingPrice;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
    }

    private final Map<Long, AuctionContext> cache = new ConcurrentHashMap<>();

    public void put(Long auctionId, AuctionContext context) {
        cache.put(auctionId, context);
    }

    public AuctionContext get(Long auctionId) {
        return cache.get(auctionId);
    }

    public void remove(Long auctionId) {
        cache.remove(auctionId);
    }
}
