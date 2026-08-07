package com.example.service;

import com.example.DTO.AuctionResponse;
import com.example.DTO.CreateAuctionRequest;
import com.example.DTO.UpdateAuctionRequest;
import com.example.exception.AuctionNotFoundException;
import com.example.exception.InvalidAuctionStateException;
import com.example.model.Auction;
import com.example.repository.AuctionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceImplTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private AuctionServiceImpl auctionService;

    @BeforeEach
    void setUp() {
        auctionService = new AuctionServiceImpl(auctionRepository, kafkaTemplate, objectMapper);
    }

    private LocalDateTime now = LocalDateTime.now();

    private CreateAuctionRequest createRequest() {
        CreateAuctionRequest request = new CreateAuctionRequest();
        request.setTitle("Vintage Watch");
        request.setDescription("Rare vintage watch");
        request.setSellerId(3L);
        request.setStartingPrice(new BigDecimal("100.00"));
        request.setStartTime(now.plusHours(1));
        request.setEndTime(now.plusDays(1));
        return request;
    }

    private Auction auctionWithId(Long id, Auction.AuctionStatus status) {
        return Auction.builder()
                .id(id)
                .title("Vintage Watch")
                .description("Rare vintage watch")
                .sellerId(3L)
                .startingPrice(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("100.00"))
                .startTime(now.plusHours(1))
                .endTime(now.plusDays(1))
                .status(status)
                .build();
    }

    @Test
    void createAuction_success_publishesEvent() {
        CreateAuctionRequest request = createRequest();
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> {
            Auction auction = invocation.getArgument(0);
            auction.setId(1L);
            return auction;
        });

        AuctionResponse response = auctionService.createAuction(request, 3L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Vintage Watch");
        assertThat(response.getStatus()).isEqualTo(Auction.AuctionStatus.PENDING);
        assertThat(response.getCurrentPrice()).isEqualByComparingTo("100.00");

        ArgumentCaptor<Auction> captor = ArgumentCaptor.forClass(Auction.class);
        verify(auctionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Auction.AuctionStatus.PENDING);
        assertThat(captor.getValue().getSellerId()).isEqualTo(3L);
        verify(kafkaTemplate).send(eq("auction-created"), eq("1"), any(String.class));
    }

    @Test
    void createAuction_endBeforeStart_throws() {
        CreateAuctionRequest request = createRequest();
        request.setEndTime(request.getStartTime().minusMinutes(5));

        assertThatThrownBy(() -> auctionService.createAuction(request, 3L))
                .isInstanceOf(InvalidAuctionStateException.class)
                .hasMessageContaining("End time must be after start time");

        verify(auctionRepository, never()).save(any());
    }

    @Test
    void getAuctionById_success() {
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auctionWithId(1L, Auction.AuctionStatus.ACTIVE)));

        AuctionResponse response = auctionService.getAuctionById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(Auction.AuctionStatus.ACTIVE);
    }

    @Test
    void getAuctionById_notFound_throws() {
        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.getAuctionById(1L))
                .isInstanceOf(AuctionNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getAllAuctions_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Auction> page = new PageImpl<>(List.of(auctionWithId(1L, Auction.AuctionStatus.ACTIVE)));
        when(auctionRepository.findAll(pageable)).thenReturn(page);

        Page<AuctionResponse> result = auctionService.getAllAuctions(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getAuctionsByStatus_delegates() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Auction> page = new PageImpl<>(List.of(auctionWithId(2L, Auction.AuctionStatus.ACTIVE)));
        when(auctionRepository.findByStatus(Auction.AuctionStatus.ACTIVE, pageable)).thenReturn(page);

        Page<AuctionResponse> result = auctionService.getAuctionsByStatus(Auction.AuctionStatus.ACTIVE, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(Auction.AuctionStatus.ACTIVE);
    }

    @Test
    void getAuctionsBySeller_delegates() {
        when(auctionRepository.findBySellerId(3L))
                .thenReturn(List.of(auctionWithId(1L, Auction.AuctionStatus.PENDING)));

        List<AuctionResponse> result = auctionService.getAuctionsBySeller(3L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSellerId()).isEqualTo(3L);
    }

    @Test
    void getActiveAuctionCount_delegates() {
        when(auctionRepository.countByStatus(Auction.AuctionStatus.ACTIVE)).thenReturn(5L);

        assertThat(auctionService.getActiveAuctionCount()).isEqualTo(5L);
    }

    @Test
    void updateAuction_success() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.PENDING);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAuctionRequest request = new UpdateAuctionRequest("New Title", null, new BigDecimal("150.00"), null, null);
        AuctionResponse response = auctionService.updateAuction(1L, request, 3L);

        assertThat(response.getTitle()).isEqualTo("New Title");
        assertThat(response.getStartingPrice()).isEqualByComparingTo("150.00");
        assertThat(response.getCurrentPrice()).isEqualByComparingTo("150.00");
    }

    @Test
    void updateAuction_notPending_throws() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        UpdateAuctionRequest request = new UpdateAuctionRequest();
        assertThatThrownBy(() -> auctionService.updateAuction(1L, request, 3L))
                .isInstanceOf(InvalidAuctionStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void updateAuction_notFound_throws() {
        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.updateAuction(1L, new UpdateAuctionRequest(), 3L))
                .isInstanceOf(AuctionNotFoundException.class);
    }

    @Test
    void updateAuction_endBeforeStart_throws() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.PENDING);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        UpdateAuctionRequest request = new UpdateAuctionRequest(null, null, null, now.plusDays(2), now.plusDays(1));
        assertThatThrownBy(() -> auctionService.updateAuction(1L, request, 3L))
                .isInstanceOf(InvalidAuctionStateException.class)
                .hasMessageContaining("End time must be after start time");
    }

    @Test
    void updateAuction_notOwner_throws() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.PENDING);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        assertThatThrownBy(() -> auctionService.updateAuction(1L, new UpdateAuctionRequest(), 99L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void startAuction_success_publishesEvent() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.PENDING);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuctionResponse response = auctionService.startAuction(1L, 3L);

        assertThat(response.getStatus()).isEqualTo(Auction.AuctionStatus.ACTIVE);
        verify(kafkaTemplate).send(eq("auction-started"), eq("1"), any(String.class));
    }

    @Test
    void startAuction_notPending_throws() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        assertThatThrownBy(() -> auctionService.startAuction(1L, 3L))
                .isInstanceOf(InvalidAuctionStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void closeAuction_success_publishesEvent() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.ACTIVE);
        auction.setHighestBidId(9L);
        auction.setHighestBidderId(7L);
        auction.setCurrentPrice(new BigDecimal("250.00"));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuctionResponse response = auctionService.closeAuction(1L, 3L);

        assertThat(response.getStatus()).isEqualTo(Auction.AuctionStatus.CLOSED);
        verify(kafkaTemplate).send(eq("auction-closed"), eq("1"), any(String.class));
    }

    @Test
    void closeAuction_notActive_throws() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.PENDING);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        assertThatThrownBy(() -> auctionService.closeAuction(1L, 3L))
                .isInstanceOf(InvalidAuctionStateException.class)
                .hasMessageContaining("ACTIVE");
    }

    @Test
    void deleteAuction_success() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.PENDING);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        auctionService.deleteAuction(1L, 3L);

        verify(auctionRepository).delete(any(Auction.class));
    }

    @Test
    void deleteAuction_notFound_throws() {
        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.deleteAuction(1L, 3L))
                .isInstanceOf(AuctionNotFoundException.class);

        verify(auctionRepository, never()).delete(any());
    }

    @Test
    void deleteAuction_notOwner_throws() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.PENDING);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        assertThatThrownBy(() -> auctionService.deleteAuction(1L, 99L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);

        verify(auctionRepository, never()).delete(any());
    }

    @Test
    void processAuctionStateTransitions_autoStartsAndCloses() {
        Auction pendingToStart = auctionWithId(1L, Auction.AuctionStatus.PENDING);
        Auction activeToClose = auctionWithId(2L, Auction.AuctionStatus.ACTIVE);
        Auction pendingToExpire = auctionWithId(3L, Auction.AuctionStatus.PENDING);

        when(auctionRepository.findByStatusAndStartTimeBefore(eq(Auction.AuctionStatus.PENDING), any()))
                .thenReturn(List.of(pendingToStart));
        when(auctionRepository.findByStatusAndEndTimeBefore(eq(Auction.AuctionStatus.ACTIVE), any()))
                .thenReturn(List.of(activeToClose));
        when(auctionRepository.findByStatusAndEndTimeBeforeAndStartTimeAfter(eq(Auction.AuctionStatus.PENDING), any(), any()))
                .thenReturn(List.of(pendingToExpire));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(pendingToStart));
        when(auctionRepository.findById(2L)).thenReturn(Optional.of(activeToClose));
        when(auctionRepository.findById(3L)).thenReturn(Optional.of(pendingToExpire));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auctionService.processAuctionStateTransitions();

        assertThat(pendingToStart.getStatus()).isEqualTo(Auction.AuctionStatus.ACTIVE);
        assertThat(activeToClose.getStatus()).isEqualTo(Auction.AuctionStatus.CLOSED);
        assertThat(pendingToExpire.getStatus()).isEqualTo(Auction.AuctionStatus.EXPIRED);

        verify(auctionRepository, times(3)).save(any(Auction.class));
        verify(kafkaTemplate).send(eq("auction-started"), eq("1"), any(String.class));
        verify(kafkaTemplate).send(eq("auction-closed"), eq("2"), any(String.class));
    }

    @Test
    void processAuctionStateTransitions_noAuctions_noEvents() {
        when(auctionRepository.findByStatusAndStartTimeBefore(eq(Auction.AuctionStatus.PENDING), any()))
                .thenReturn(List.of());
        when(auctionRepository.findByStatusAndEndTimeBefore(eq(Auction.AuctionStatus.ACTIVE), any()))
                .thenReturn(List.of());
        when(auctionRepository.findByStatusAndEndTimeBeforeAndStartTimeAfter(eq(Auction.AuctionStatus.PENDING), any(), any()))
                .thenReturn(List.of());

        auctionService.processAuctionStateTransitions();

        verify(auctionRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(String.class), any(String.class));
    }

    @Test
    void recordBid_higherBid_updatesHighestBid() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.ACTIVE);
        auction.setCurrentPrice(new BigDecimal("100.00"));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auctionService.recordBid(1L, 9L, 7L, new BigDecimal("250.00"));

        assertThat(auction.getHighestBidId()).isEqualTo(9L);
        assertThat(auction.getHighestBidderId()).isEqualTo(7L);
        assertThat(auction.getCurrentPrice()).isEqualByComparingTo("250.00");
        verify(auctionRepository).save(auction);
    }

    @Test
    void recordBid_lowerOrEqualBid_isIgnored() {
        Auction auction = auctionWithId(1L, Auction.AuctionStatus.ACTIVE);
        auction.setCurrentPrice(new BigDecimal("100.00"));
        auction.setHighestBidId(3L);
        auction.setHighestBidderId(4L);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        auctionService.recordBid(1L, 5L, 6L, new BigDecimal("100.00"));

        assertThat(auction.getHighestBidId()).isEqualTo(3L);
        assertThat(auction.getHighestBidderId()).isEqualTo(4L);
        assertThat(auction.getCurrentPrice()).isEqualByComparingTo("100.00");
        verify(auctionRepository, never()).save(any());
    }

    @Test
    void recordBid_nonActiveOrMissingAuction_isIgnored() {
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auctionWithId(1L, Auction.AuctionStatus.CLOSED)));
        auctionService.recordBid(1L, 9L, 7L, new BigDecimal("250.00"));
        verify(auctionRepository, never()).save(any());

        when(auctionRepository.findById(2L)).thenReturn(Optional.empty());
        auctionService.recordBid(2L, 9L, 7L, new BigDecimal("250.00"));
        verify(auctionRepository, never()).save(any());
    }
}
