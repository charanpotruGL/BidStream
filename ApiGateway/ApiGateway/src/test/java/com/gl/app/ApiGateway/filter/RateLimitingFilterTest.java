package com.gl.app.ApiGateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    @Mock
    private WebFilterChain chain;

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private ServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/auctions").build());
    }

    @Test
    void requestUnderLimit_continues() {
        when(valueOps.increment("rate:unknown")).thenReturn(Mono.just(5L));
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange(), chain)).verifyComplete();

        verify(chain).filter(any());
        assertThat(exchange().getResponse().getStatusCode()).isNull();
    }

    @Test
    void firstRequest_setsExpiryWindow() {
        when(valueOps.increment("rate:unknown")).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(eq("rate:unknown"), any(Duration.class))).thenReturn(Mono.just(true));
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange(), chain)).verifyComplete();

        verify(redisTemplate).expire("rate:unknown", Duration.ofMinutes(1));
        verify(chain).filter(any());
    }

    @Test
    void requestOverLimit_returnsTooManyRequests() {
        when(valueOps.increment("rate:unknown")).thenReturn(Mono.just(101L));

        ServerWebExchange exchange = exchange();
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        verify(chain, never()).filter(any());
    }

    @Test
    void exactlyAtLimit_continues() {
        when(valueOps.increment("rate:unknown")).thenReturn(Mono.just(100L));
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange(), chain)).verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void redisError_doesNotBlockRequest() {
        when(valueOps.increment(anyString())).thenReturn(Mono.error(new RuntimeException("redis down")));
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange(), chain)).verifyComplete();

        verify(chain).filter(any());
        assertThat(exchange().getResponse().getStatusCode()).isNull();
    }

    @Test
    void forwardedForHeader_usesFirstClientIp() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/auctions")
                        .header("X-Forwarded-For", "203.0.113.7, 10.0.0.1")
                        .build());
        when(valueOps.increment("rate:203.0.113.7")).thenReturn(Mono.just(2L));
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(valueOps).increment("rate:203.0.113.7");
        verify(chain).filter(any());
    }
}
