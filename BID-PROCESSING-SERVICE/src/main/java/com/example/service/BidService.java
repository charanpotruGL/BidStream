package com.example.service;

import com.example.DTO.BidResponse;
import com.example.DTO.CreateBidRequest;
import com.example.model.Bid;

import java.util.List;

public interface BidService {

    BidResponse placeBid(CreateBidRequest request);

    void markAuctionOutcome(Long auctionId, Long winningBidId);

    BidResponse getBidById(Long bidId);

    List<BidResponse> getBidsByAuction(Long auctionId);

    List<BidResponse> getBidsByBidder(Long bidderId);

    BidResponse getHighestBidForAuction(Long auctionId);

    Bid getBidEntity(Long bidId);
}
