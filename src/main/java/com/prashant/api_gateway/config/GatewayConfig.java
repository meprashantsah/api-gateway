package com.prashant.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.prashant.api_gateway.filter.AuthenticationFilter;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authFilter;

    public GatewayConfig(AuthenticationFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth-service/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("authServiceCircuitBreaker").setFallbackUri("forward:/fallback/auth"))
                                .rewritePath("/auth-service/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri("lb://auth-service"))
                .route("user-service", r -> r.path("/user-service/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("userServiceCircuitBreaker").setFallbackUri("forward:/fallback/users"))
                                .rewritePath("/user-service/(?<segment>.*)", "/api/users/${segment}"))
                        .uri("lb://user-service"))
                .route("chat-service-websocket", r -> r.path("/chat-service/ws/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .rewritePath("/chat-service/ws/(?<segment>.*)", "/ws/${segment}"))
                        .uri("lb://chat-service"))
                .route("chat-service", r -> r.path("/chat-service/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("chatServiceCircuitBreaker").setFallbackUri("forward:/fallback/chat"))
                                .rewritePath("/chat-service/(?<segment>.*)", "/api/chat/${segment}"))
                        .uri("lb://chat-service"))
                .build();
    }
}