package com.example.repository;

import com.example.model.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    Page<Auction> findByStatus(Auction.AuctionStatus status, Pageable pageable);

    List<Auction> findBySellerId(Long sellerId);

    @Query("SELECT COUNT(a) FROM Auction a WHERE a.status = :status")
    long countByStatus(@Param("status") Auction.AuctionStatus status);

    Optional<Auction> findFirstByHighestBidderId(Long bidderId);

    List<Auction> findByStatusAndStartTimeBefore(Auction.AuctionStatus status, LocalDateTime time);

    List<Auction> findByStatusAndEndTimeBefore(Auction.AuctionStatus status, LocalDateTime time);
}
