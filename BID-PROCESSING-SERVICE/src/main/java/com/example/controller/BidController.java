package com.example.controller;

import com.example.DTO.BidResponse;
import com.example.DTO.CreateBidRequest;
import com.example.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<BidResponse> placeBid(@Valid @RequestBody CreateBidRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bidService.placeBid(request));
    }

    @GetMapping("/{bidId}")
    public ResponseEntity<BidResponse> getBidById(@PathVariable Long bidId) {
        return ResponseEntity.ok(bidService.getBidById(bidId));
    }

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidResponse>> getBidsByAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(bidService.getBidsByAuction(auctionId));
    }

    @GetMapping("/bidder/{bidderId}")
    public ResponseEntity<List<BidResponse>> getBidsByBidder(@PathVariable Long bidderId) {
        return ResponseEntity.ok(bidService.getBidsByBidder(bidderId));
    }

    @GetMapping("/auction/{auctionId}/highest")
    public ResponseEntity<BidResponse> getHighestBidForAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(bidService.getHighestBidForAuction(auctionId));
    }
}
