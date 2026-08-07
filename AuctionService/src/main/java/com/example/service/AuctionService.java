package com.example.service;

import com.example.DTO.AuctionResponse;
import com.example.DTO.CreateAuctionRequest;
import com.example.DTO.UpdateAuctionRequest;
import com.example.model.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface AuctionService {

    AuctionResponse createAuction(CreateAuctionRequest request, Long sellerId);

    AuctionResponse getAuctionById(Long id);

    Page<AuctionResponse> getAllAuctions(Pageable pageable);

    Page<AuctionResponse> getAuctionsByStatus(Auction.AuctionStatus status, Pageable pageable);

    List<AuctionResponse> getAuctionsBySeller(Long sellerId);

    long getActiveAuctionCount();

    AuctionResponse updateAuction(Long id, UpdateAuctionRequest request, Long sellerId);

    AuctionResponse startAuction(Long id, Long sellerId);

    AuctionResponse closeAuction(Long id, Long sellerId);

    void deleteAuction(Long id, Long sellerId);

    Auction getAuctionEntity(Long id);

    void recordBid(Long auctionId, Long bidId, Long bidderId, BigDecimal amount);
}
