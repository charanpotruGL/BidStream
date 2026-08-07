package com.example.service;

import com.example.DTO.BidResponse;
import com.example.DTO.CreateBidRequest;
import com.example.exception.BidNotFoundException;
import com.example.exception.InvalidBidException;
import com.example.model.Bid;
import com.example.repository.BidRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidServiceImplTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private AuctionClient auctionClient;

    private final AuctionTitleCache auctionTitleCache = new AuctionTitleCache();
    private final AuctionContextCache auctionContextCache = new AuctionContextCache();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private BidServiceImpl bidService;

    @BeforeEach
    void setUp() {
        bidService = new BidServiceImpl(bidRepository, kafkaTemplate, objectMapper, auctionTitleCache, auctionContextCache, auctionClient);
        lenient().when(bidRepository.lockAuctionById(anyLong())).thenReturn(List.of());
        auctionContextCache.put(1L, context("ACTIVE", "100.00", 9L, LocalDateTime.now().plusDays(1)));
    }

    private AuctionContextCache.AuctionContext context(String status, String startingPrice, Long sellerId, LocalDateTime endTime) {
        return AuctionContextCache.AuctionContext.builder()
                .title("Vintage Watch")
                .sellerId(sellerId)
                .startingPrice(new BigDecimal(startingPrice))
                .endTime(endTime)
                .status(status)
                .build();
    }

    private CreateBidRequest bidRequest(Long auctionId, Long bidderId, String amount) {
        CreateBidRequest request = new CreateBidRequest();
        request.setAuctionId(auctionId);
        request.setBidderId(bidderId);
        request.setAmount(new BigDecimal(amount));
        return request;
    }

    private Bid bid(Long id, Long auctionId, Long bidderId, String amount, Bid.BidStatus status) {
        return Bid.builder()
                .id(id)
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(new BigDecimal(amount))
                .status(status)
                .build();
    }

    @Test
    void placeBid_publishesEventWithAuctionTitle() throws Exception {
        auctionTitleCache.put(1L, "Antique Vase");
        CreateBidRequest request = bidRequest(1L, 10L, "150.00");
        when(bidRepository.findFirstByAuctionIdAndStatusOrderByAmountDesc(1L, Bid.BidStatus.PLACED))
                .thenReturn(Optional.empty());
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> {
            Bid bid = invocation.getArgument(0);
            bid.setId(1L);
            return bid;
        });

        bidService.placeBid(request);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("bid-placed"), eq("1"), captor.capture());
        assertThat(captor.getValue()).contains("\"auctionTitle\":\"Antique Vase\"");
    }

    @Test
    void placeBid_firstBid_success() {
        CreateBidRequest request = bidRequest(1L, 10L, "150.00");
        when(bidRepository.findFirstByAuctionIdAndStatusOrderByAmountDesc(1L, Bid.BidStatus.PLACED))
                .thenReturn(Optional.empty());
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> {
            Bid bid = invocation.getArgument(0);
            bid.setId(1L);
            return bid;
        });

        BidResponse response = bidService.placeBid(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAuctionId()).isEqualTo(1L);
        assertThat(response.getBidderId()).isEqualTo(10L);
        assertThat(response.getAmount()).isEqualByComparingTo("150.00");
        assertThat(response.getStatus()).isEqualTo(Bid.BidStatus.PLACED);
        verify(bidRepository).lockAuctionById(1L);
        verify(kafkaTemplate).send(eq("bid-placed"), eq("1"), any(String.class));
    }

    @Test
    void placeBid_higherBid_outbidsPrevious() {
        CreateBidRequest request = bidRequest(1L, 10L, "200.00");
        Bid highest = bid(5L, 1L, 9L, "100.00", Bid.BidStatus.PLACED);
        when(bidRepository.findFirstByAuctionIdAndStatusOrderByAmountDesc(1L, Bid.BidStatus.PLACED))
                .thenReturn(Optional.of(highest));
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> {
            Bid bid = invocation.getArgument(0);
            if (bid.getId() == null) {
                bid.setId(6L);
            }
            return bid;
        });

        BidResponse response = bidService.placeBid(request);

        assertThat(response.getId()).isEqualTo(6L);
        assertThat(response.getAmount()).isEqualByComparingTo("200.00");
        assertThat(highest.getStatus()).isEqualTo(Bid.BidStatus.OUTBID);
        verify(bidRepository).save(highest);
        verify(kafkaTemplate).send(eq("bid-outbid"), eq("1"), any(String.class));
        verify(kafkaTemplate).send(eq("bid-placed"), eq("1"), any(String.class));
    }

    @Test
    void placeBid_lowerThanHighest_throws() {
        CreateBidRequest request = bidRequest(1L, 10L, "100.00");
        Bid highest = bid(5L, 1L, 9L, "100.00", Bid.BidStatus.PLACED);
        when(bidRepository.findFirstByAuctionIdAndStatusOrderByAmountDesc(1L, Bid.BidStatus.PLACED))
                .thenReturn(Optional.of(highest));

        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(InvalidBidException.class)
                .hasMessageContaining("higher than the current price");

        verify(bidRepository, never()).save(any());
    }

    @Test
    void placeBid_belowStartingPrice_throws() {
        CreateBidRequest request = bidRequest(1L, 10L, "50.00");

        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(InvalidBidException.class)
                .hasMessageContaining("higher than the current price");

        verify(bidRepository, never()).save(any());
    }

    @Test
    void placeBid_notActiveAuction_throws() {
        auctionContextCache.put(2L, context("PENDING", "100.00", 9L, LocalDateTime.now().plusDays(1)));
        CreateBidRequest request = bidRequest(2L, 10L, "150.00");

        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(InvalidBidException.class)
                .hasMessageContaining("not accepting bids");

        verify(bidRepository, never()).save(any());
    }

    @Test
    void placeBid_endedAuction_throws() {
        auctionContextCache.put(2L, context("ACTIVE", "100.00", 9L, LocalDateTime.now().minusDays(1)));
        CreateBidRequest request = bidRequest(2L, 10L, "150.00");

        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(InvalidBidException.class)
                .hasMessageContaining("already ended");

        verify(bidRepository, never()).save(any());
    }

    @Test
    void placeBid_sellerBidding_throws() {
        CreateBidRequest request = bidRequest(1L, 9L, "150.00");

        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(InvalidBidException.class)
                .hasMessageContaining("your own auction");

        verify(bidRepository, never()).save(any());
    }

    @Test
    void placeBid_unknownAuction_throws() {
        CreateBidRequest request = bidRequest(99L, 10L, "150.00");

        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(InvalidBidException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void placeBid_cacheMiss_fetchesContextFromAuctionService() {
        CreateBidRequest request = bidRequest(7L, 10L, "150.00");
        when(auctionClient.fetchContext(7L)).thenReturn(
                AuctionContextCache.AuctionContext.builder()
                        .title("Fetched Auction")
                        .sellerId(9L)
                        .startingPrice(new BigDecimal("100.00"))
                        .endTime(LocalDateTime.now().plusDays(1))
                        .status("ACTIVE")
                        .build());
        when(bidRepository.findFirstByAuctionIdAndStatusOrderByAmountDesc(7L, Bid.BidStatus.PLACED))
                .thenReturn(Optional.empty());
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> {
            Bid bid = invocation.getArgument(0);
            bid.setId(1L);
            return bid;
        });

        BidResponse response = bidService.placeBid(request);

        assertThat(response.getAuctionId()).isEqualTo(7L);
        assertThat(auctionContextCache.get(7L)).isNotNull();
        assertThat(auctionTitleCache.get(7L)).isEqualTo("Fetched Auction");
        verify(auctionClient).fetchContext(7L);
    }

    @Test
    void placeBid_zeroAmount_throws() {
        CreateBidRequest request = bidRequest(1L, 10L, "0.00");

        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(InvalidBidException.class)
                .hasMessageContaining("greater than zero");

        verify(bidRepository, never()).findFirstByAuctionIdAndStatusOrderByAmountDesc(any(), any());
    }

    @Test
    void placeBid_negativeAmount_throws() {
        CreateBidRequest request = bidRequest(1L, 10L, "-5.00");

        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(InvalidBidException.class);
    }

    @Test
    void placeBid_missingAuctionId_throws() {
        CreateBidRequest request = bidRequest(null, 10L, "50.00");

        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(InvalidBidException.class)
                .hasMessageContaining("Auction ID and Bidder ID are required");
    }

    @Test
    void markAuctionOutcome_marksWinnerAndLosers() {
        Bid winner = bid(5L, 1L, 10L, "200.00", Bid.BidStatus.PLACED);
        Bid loser = bid(4L, 1L, 11L, "100.00", Bid.BidStatus.PLACED);
        Bid outbid = bid(3L, 1L, 9L, "50.00", Bid.BidStatus.OUTBID);
        when(bidRepository.findByAuctionIdOrderByIdAsc(1L)).thenReturn(List.of(outbid, loser, winner));

        bidService.markAuctionOutcome(1L, 5L);

        assertThat(winner.getStatus()).isEqualTo(Bid.BidStatus.WINNING);
        assertThat(loser.getStatus()).isEqualTo(Bid.BidStatus.LOST);
        assertThat(outbid.getStatus()).isEqualTo(Bid.BidStatus.OUTBID);
        verify(bidRepository).saveAll(anyList());
    }

    @Test
    void markAuctionOutcome_noWinner_marksAllLost() {
        Bid first = bid(4L, 1L, 11L, "100.00", Bid.BidStatus.PLACED);
        when(bidRepository.findByAuctionIdOrderByIdAsc(1L)).thenReturn(List.of(first));

        bidService.markAuctionOutcome(1L, null);

        assertThat(first.getStatus()).isEqualTo(Bid.BidStatus.LOST);
        verify(bidRepository).saveAll(anyList());
    }

    @Test
    void getBidById_success() {
        when(bidRepository.findById(3L)).thenReturn(Optional.of(bid(3L, 1L, 10L, "50.00", Bid.BidStatus.PLACED)));

        BidResponse response = bidService.getBidById(3L);

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getBidderId()).isEqualTo(10L);
    }

    @Test
    void getBidById_notFound_throws() {
        when(bidRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bidService.getBidById(3L))
                .isInstanceOf(BidNotFoundException.class)
                .hasMessageContaining("3");
    }

    @Test
    void getBidsByAuction_returnsList() {
        when(bidRepository.findByAuctionIdOrderByIdAsc(1L)).thenReturn(List.of(
                bid(1L, 1L, 10L, "50.00", Bid.BidStatus.PLACED),
                bid(2L, 1L, 11L, "60.00", Bid.BidStatus.PLACED)));

        List<BidResponse> result = bidService.getBidsByAuction(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BidResponse::getAuctionId).containsOnly(1L);
    }

    @Test
    void getBidsByBidder_returnsList() {
        when(bidRepository.findByBidderIdOrderByIdDesc(10L)).thenReturn(List.of(
                bid(1L, 1L, 10L, "50.00", Bid.BidStatus.PLACED)));

        List<BidResponse> result = bidService.getBidsByBidder(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBidderId()).isEqualTo(10L);
    }

    @Test
    void getHighestBidForAuction_success() {
        when(bidRepository.findFirstByAuctionIdOrderByAmountDesc(1L))
                .thenReturn(Optional.of(bid(1L, 1L, 10L, "500.00", Bid.BidStatus.PLACED)));

        BidResponse response = bidService.getHighestBidForAuction(1L);

        assertThat(response.getAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    void getHighestBidForAuction_none_throws() {
        when(bidRepository.findFirstByAuctionIdOrderByAmountDesc(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bidService.getHighestBidForAuction(1L))
                .isInstanceOf(BidNotFoundException.class)
                .hasMessageContaining("No bids found");
    }
}
