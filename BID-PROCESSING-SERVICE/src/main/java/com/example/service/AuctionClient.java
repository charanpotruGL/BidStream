package com.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionClient {

    private final ObjectMapper objectMapper;

    @Value("${auction.service.url}")
    private String auctionServiceUrl;

    public AuctionContextCache.AuctionContext fetchContext(Long auctionId) {
        if (auctionId == null) {
            return null;
        }
        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(auctionServiceUrl + "/api/auctions/internal")
                    .build();
            ResponseEntity<String> response = restClient.get()
                    .uri("/{id}", auctionId)
                    .retrieve()
                    .toEntity(String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            return toContext(response.getBody());
        } catch (Exception e) {
            log.warn("Failed to fetch auction context for auctionId={} from {}: {}",
                    auctionId, auctionServiceUrl, e.getMessage());
            return null;
        }
    }

    private AuctionContextCache.AuctionContext toContext(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return AuctionContextCache.AuctionContext.builder()
                    .title(node.path("title").asText(null))
                    .sellerId(node.path("sellerId").isNull() ? null : node.path("sellerId").asLong())
                    .startingPrice(toBigDecimal(node.path("startingPrice")))
                    .startTime(toLocalDateTime(node.path("startTime")))
                    .endTime(toLocalDateTime(node.path("endTime")))
                    .status(node.path("status").asText(null))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse auction context response: {}", json, e);
            return null;
        }
    }

    private BigDecimal toBigDecimal(JsonNode node) {
        if (node.isNull() || node.isMissingNode() || node.asText().isEmpty()) {
            return null;
        }
        return node.isNumber() ? node.decimalValue() : new BigDecimal(node.asText());
    }

    private LocalDateTime toLocalDateTime(JsonNode node) {
        if (node.isNull() || node.isMissingNode() || node.asText().isEmpty()) {
            return null;
        }
        if (node.isTextual()) {
            return LocalDateTime.parse(node.asText());
        }
        if (node.isArray()) {
            return LocalDateTime.of(
                    node.get(0).asInt(), node.get(1).asInt(), node.get(2).asInt(),
                    node.get(3).asInt(), node.get(4).asInt(), node.get(5).asInt(),
                    node.get(6) == null ? 0 : node.get(6).asInt());
        }
        return null;
    }
}
