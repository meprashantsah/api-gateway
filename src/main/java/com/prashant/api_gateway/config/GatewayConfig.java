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
                .route("auth-service", r -> r.path("/auth-service/**") //change from .route("auth-service", r -> r.path("/api//auth/**") so we can use /auth-service instead of /api/auth
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("authServiceCircuitBreaker").setFallbackUri("forward:/fallback/auth"))
                                .rewritePath("/auth-service/(?<segment>.*)", "/api/auth/${segment}"))   // change from .rewritePath("/api/auth/(?<segment>.*)", "/api/auth/${segment}")) so we can use /auth-service instead of /api/auth
                        .uri("lb://auth-service"))
                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("orderServiceCircuitBreaker").setFallbackUri("forward:/fallback/orders"))
                                .rewritePath("/api/orders/(?<segment>.*)", "/api/orders/${segment}"))
                        .uri("lb://order-service"))
                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("productServiceCircuitBreaker").setFallbackUri("forward:/fallback/products"))
                                .rewritePath("/api/products/(?<segment>.*)", "/api/products/${segment}"))
                        .uri("lb://product-service"))
                .route("admin-service", r -> r.path("/api/admin/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("adminServiceCircuitBreaker").setFallbackUri("forward:/fallback/admin"))
                                .rewritePath("/api/admin/(?<segment>.*)", "/api/admin/${segment}"))
                        .uri("lb://admin-service"))
                .build();
    }
}

