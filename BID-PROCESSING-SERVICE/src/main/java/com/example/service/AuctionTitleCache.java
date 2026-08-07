package com.example.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuctionTitleCache {

    private final ConcurrentHashMap<Long, String> titles = new ConcurrentHashMap<>();

    public void put(Long auctionId, String title) {
        if (auctionId != null && title != null) {
            titles.put(auctionId, title);
        }
    }

    public String get(Long auctionId) {
        return auctionId == null ? null : titles.get(auctionId);
    }
}
