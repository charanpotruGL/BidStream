package com.example.service;

import com.example.DTO.AuctionResponse;
import com.example.DTO.CreateAuctionRequest;
import com.example.DTO.UpdateAuctionRequest;
import com.example.model.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuctionService {

    AuctionResponse createAuction(CreateAuctionRequest request);

    AuctionResponse getAuctionById(Long id);

    Page<AuctionResponse> getAllAuctions(Pageable pageable);

    Page<AuctionResponse> getAuctionsByStatus(Auction.AuctionStatus status, Pageable pageable);

    List<AuctionResponse> getAuctionsBySeller(Long sellerId);

    long getActiveAuctionCount();

    AuctionResponse updateAuction(Long id, UpdateAuctionRequest request);

    AuctionResponse startAuction(Long id);

    AuctionResponse closeAuction(Long id);

    void deleteAuction(Long id);

    Auction getAuctionEntity(Long id);
}
