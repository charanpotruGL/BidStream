package com.example.controller;

import com.example.DTO.AuctionResponse;
import com.example.DTO.CreateAuctionRequest;
import com.example.DTO.UpdateAuctionRequest;
import com.example.model.Auction;
import com.example.service.AuctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionControllerTest {

    @Mock
    private AuctionService auctionService;

    private AuctionController auctionController;

    @BeforeEach
    void setUp() {
        auctionController = new AuctionController(auctionService);
    }

    private AuctionResponse response() {
        return AuctionResponse.builder()
                .id(1L)
                .title("Vintage Watch")
                .sellerId(3L)
                .startingPrice(new BigDecimal("100.00"))
                .status(Auction.AuctionStatus.PENDING)
                .build();
    }

    @Test
    void createAuction_returnsCreated() {
        CreateAuctionRequest request = new CreateAuctionRequest();
        when(auctionService.createAuction(request, 3L)).thenReturn(response());

        ResponseEntity<AuctionResponse> response = auctionController.createAuction(request, 3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getAllAuctions_returnsOk() {
        Page<AuctionResponse> page = new PageImpl<>(List.of(response()));
        when(auctionService.getAllAuctions(any())).thenReturn(page);

        ResponseEntity<Page<AuctionResponse>> response = auctionController.getAllAuctions(0, 10, "id", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getAllAuctions_descDirectionMapsSort() {
        Page<AuctionResponse> page = new PageImpl<>(List.of(response()));
        when(auctionService.getAllAuctions(any())).thenReturn(page);

        ResponseEntity<Page<AuctionResponse>> response = auctionController.getAllAuctions(0, 10, "id", "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAuctionById_returnsOk() {
        when(auctionService.getAuctionById(1L)).thenReturn(response());

        ResponseEntity<AuctionResponse> response = auctionController.getAuctionById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getAuctionsByStatus_returnsOk() {
        Page<AuctionResponse> page = new PageImpl<>(List.of(response()));
        when(auctionService.getAuctionsByStatus(eq(Auction.AuctionStatus.ACTIVE), any())).thenReturn(page);

        ResponseEntity<Page<AuctionResponse>> response =
                auctionController.getAuctionsByStatus(Auction.AuctionStatus.ACTIVE, 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getAuctionsBySeller_returnsOk() {
        when(auctionService.getAuctionsBySeller(3L)).thenReturn(List.of(response()));

        ResponseEntity<List<AuctionResponse>> response = auctionController.getAuctionsBySeller(3L, 3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getAuctionsBySeller_notOwner_forbidden() {
        ResponseEntity<List<AuctionResponse>> response = auctionController.getAuctionsBySeller(3L, 99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getActiveAuctionCount_returnsOk() {
        when(auctionService.getActiveAuctionCount()).thenReturn(5L);

        ResponseEntity<Long> response = auctionController.getActiveAuctionCount();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(5L);
    }

    @Test
    void updateAuction_returnsOk() {
        UpdateAuctionRequest request = new UpdateAuctionRequest();
        when(auctionService.updateAuction(1L, request, 3L)).thenReturn(response());

        ResponseEntity<AuctionResponse> response = auctionController.updateAuction(1L, request, 3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void startAuction_returnsOk() {
        when(auctionService.startAuction(1L, 3L)).thenReturn(response());

        ResponseEntity<AuctionResponse> response = auctionController.startAuction(1L, 3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void closeAuction_returnsOk() {
        when(auctionService.closeAuction(1L, 3L)).thenReturn(response());

        ResponseEntity<AuctionResponse> response = auctionController.closeAuction(1L, 3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void deleteAuction_returnsNoContent() {
        ResponseEntity<Void> response = auctionController.deleteAuction(1L, 3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(auctionService).deleteAuction(1L, 3L);
    }
}
