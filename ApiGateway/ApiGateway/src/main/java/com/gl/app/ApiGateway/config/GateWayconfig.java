package com.gl.app.ApiGateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GateWayconfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/users/**", "/api/auth/**")
                        .uri("lb://user-service"))
                .route("auction-service", r -> r
                        .path("/api/auctions/**")
                        .uri("lb://auction-service"))
                .route("bid-service", r -> r
                        .path("/api/bids/**")
                        .uri("lb://bid-processing-service"))
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("lb://notification-service"))
                .build();
    }
}
