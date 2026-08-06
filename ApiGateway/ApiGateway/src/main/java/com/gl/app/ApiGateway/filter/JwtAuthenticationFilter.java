package com.gl.app.ApiGateway.filter;

import com.gl.app.ApiGateway.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Permit public endpoints
        if (path.startsWith("/api/auth/") || path.startsWith("/actuator") || path.startsWith("/eureka")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.isTokenValid(token)) {
            return unauthorized(exchange);
        }

        // Extract userId and role, add to headers for downstream services
        Long userId = jwtTokenProvider.extractUserId(token);
        String role = jwtTokenProvider.extractRole(token);
        String username = jwtTokenProvider.extractUsername(token);

        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r
                        .header("X-User-Id", userId != null ? userId.toString() : "")
                        .header("X-User-Role", role != null ? role : ""))
                .build();

        var authorities = role != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                : List.<SimpleGrantedAuthority>of();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        return chain.filter(mutated)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
