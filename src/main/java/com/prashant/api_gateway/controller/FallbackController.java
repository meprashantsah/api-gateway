package com.prashant.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    // The circuit breaker forwards the ORIGINAL request (method, body) here, so
    // the fallback must accept every HTTP method - not just GET. Otherwise a
    // failed POST lands on an unmapped GET handler and returns 405 instead of 503.
    @RequestMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createFallbackResponse("Auth Service", "Authentication service is temporarily unavailable."));
    }

    @RequestMapping("/users")
    public ResponseEntity<Map<String, Object>> usersFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createFallbackResponse("User Service", "User service is temporarily unavailable."));
    }

    @RequestMapping("/chat")
    public ResponseEntity<Map<String, Object>> chatFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createFallbackResponse("Chat Service", "Chat service is temporarily unavailable."));
    }

    private Map<String, Object> createFallbackResponse(String service, String message) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "service", service,
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Unavailable",
                "message", message,
                "fallback", true
        );
    }
}