package com.prashant.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createFallbackResponse("Auth Service", "Authentication service is temporarily unavailable."));
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> usersFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createFallbackResponse("User Service", "User service is temporarily unavailable."));
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