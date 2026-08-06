package com.example.repository;

import com.example.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByAuctionId(Long auctionId);

    List<Bid> findByBidderId(Long bidderId);

    Optional<Bid> findFirstByAuctionIdOrderByAmountDesc(Long auctionId);

    Optional<Bid> findFirstByAuctionIdAndStatusOrderByAmountDesc(Long auctionId, Bid.BidStatus status);
}
