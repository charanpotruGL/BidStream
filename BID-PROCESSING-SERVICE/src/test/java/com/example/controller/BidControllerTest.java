package com.example.controller;

import com.example.DTO.BidResponse;
import com.example.DTO.CreateBidRequest;
import com.example.model.Bid;
import com.example.service.BidService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidControllerTest {

    @Mock
    private BidService bidService;

    private BidController bidController;

    @BeforeEach
    void setUp() {
        bidController = new BidController(bidService);
    }

    private BidResponse response() {
        return BidResponse.builder()
                .id(1L)
                .auctionId(1L)
                .bidderId(10L)
                .amount(new BigDecimal("150.00"))
                .status(Bid.BidStatus.PLACED)
                .build();
    }

    @Test
    void placeBid_setsBidderFromPrincipal_returnsCreated() {
        CreateBidRequest request = new CreateBidRequest(1L, null, new BigDecimal("150.00"));
        when(bidService.placeBid(request)).thenReturn(response());

        ResponseEntity<BidResponse> response = bidController.placeBid(request, 10L);

        assertThat(request.getBidderId()).isEqualTo(10L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getBidById_returnsOk() {
        when(bidService.getBidById(1L)).thenReturn(response());

        ResponseEntity<BidResponse> response = bidController.getBidById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getBidsByAuction_returnsOk() {
        when(bidService.getBidsByAuction(1L)).thenReturn(List.of(response()));

        ResponseEntity<List<BidResponse>> response = bidController.getBidsByAuction(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getBidsByBidder_returnsOkForSelf() {
        when(bidService.getBidsByBidder(10L)).thenReturn(List.of(response()));

        ResponseEntity<List<BidResponse>> response = bidController.getBidsByBidder(10L, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getBidsByBidder_forbiddenForOtherUser() {
        assertThatThrownBy(() -> bidController.getBidsByBidder(10L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void getHighestBidForAuction_returnsOk() {
        when(bidService.getHighestBidForAuction(1L)).thenReturn(response());

        ResponseEntity<BidResponse> response = bidController.getHighestBidForAuction(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAuctionId()).isEqualTo(1L);
    }
}
