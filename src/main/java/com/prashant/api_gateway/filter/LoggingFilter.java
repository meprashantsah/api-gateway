package com.prashant.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        Instant start = Instant.now();

        log.info("[REQUEST] {} {} | Remote: {} | CorrelationId: {}",
                method, path, exchange.getRequest().getRemoteAddress(), correlationId);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    Duration duration = Duration.between(start, Instant.now());
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value() : 0;
                    if (statusCode >= 400) {
                        log.warn("[RESPONSE] {} {} | Status: {} | Duration: {}ms | CorrelationId: {}",
                                method, path, statusCode, duration.toMillis(), correlationId);
                    } else {
                        log.info("[RESPONSE] {} {} | Status: {} | Duration: {}ms | CorrelationId: {}",
                                method, path, statusCode, duration.toMillis(), correlationId);
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
