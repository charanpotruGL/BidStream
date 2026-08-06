package com.example.controller;

import com.example.DTO.AuctionResponse;
import com.example.DTO.CreateAuctionRequest;
import com.example.DTO.UpdateAuctionRequest;
import com.example.model.Auction;
import com.example.service.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<AuctionResponse> createAuction(@Valid @RequestBody CreateAuctionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auctionService.createAuction(request));
    }

    @GetMapping
    public ResponseEntity<Page<AuctionResponse>> getAllAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        return ResponseEntity.ok(auctionService.getAllAuctions(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuctionById(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuctionById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AuctionResponse>> getAuctionsByStatus(
            @PathVariable Auction.AuctionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auctionService.getAuctionsByStatus(status, pageable));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<AuctionResponse>> getAuctionsBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(auctionService.getAuctionsBySeller(sellerId));
    }

    @GetMapping("/stats/active-count")
    public ResponseEntity<Long> getActiveAuctionCount() {
        return ResponseEntity.ok(auctionService.getActiveAuctionCount());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionResponse> updateAuction(
            @PathVariable Long id, @Valid @RequestBody UpdateAuctionRequest request) {
        return ResponseEntity.ok(auctionService.updateAuction(id, request));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<AuctionResponse> startAuction(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.startAuction(id));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<AuctionResponse> closeAuction(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.closeAuction(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuction(@PathVariable Long id) {
        auctionService.deleteAuction(id);
        return ResponseEntity.noContent().build();
    }
}
