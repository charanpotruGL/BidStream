package com.example.repository;

import com.example.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByAuctionIdOrderByIdAsc(Long auctionId);

    List<Bid> findByBidderIdOrderByIdDesc(Long bidderId);

    Optional<Bid> findFirstByAuctionIdOrderByAmountDesc(Long auctionId);

    Optional<Bid> findFirstByAuctionIdAndStatusOrderByAmountDesc(Long auctionId, Bid.BidStatus status);

    @Query(value = "SELECT pg_advisory_xact_lock(CAST(:auctionId AS bigint))", nativeQuery = true)
    List<Object> lockAuctionById(@org.springframework.data.repository.query.Param("auctionId") Long auctionId);
}
