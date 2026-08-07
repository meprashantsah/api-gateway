package com.prashant.api_gateway.filter;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.prashant.api_gateway.security.JwtTokenValidator;
import com.prashant.api_gateway.security.PublicPathMatcher;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtTokenValidator jwtTokenValidator;
    private final PublicPathMatcher publicPathMatcher;

    public AuthenticationFilter(JwtTokenValidator jwtTokenValidator, PublicPathMatcher publicPathMatcher) {
        super(Config.class);
        this.jwtTokenValidator = jwtTokenValidator;
        this.publicPathMatcher = publicPathMatcher;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            if (publicPathMatcher.isPublic(request)) {
                log.debug("Public path accessed: {}", path);
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = jwtTokenValidator.validateToken(token);
                String userId = jwtTokenValidator.getUserId(claims);
                String username = jwtTokenValidator.getUsername(claims);
                List<String> roles = jwtTokenValidator.getRoles(claims);
                String rolesHeader = roles != null ? String.join(",", roles) : "";

                log.debug("Authenticated user: {} ({}), roles: {}", username, userId, rolesHeader);

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-Username", username != null ? username : "")
                        .header("X-Roles", rolesHeader)
                        .header("X-Token-Valid", "true")
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (JwtTokenValidator.JwtValidationException e) {
                log.warn("JWT validation failed for path {}: {}", path, e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED, e.getMessage());
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        String body = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"traceId\":\"%s\"}",
                java.time.Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                correlationId != null ? correlationId : ""
        );
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
    }
}

