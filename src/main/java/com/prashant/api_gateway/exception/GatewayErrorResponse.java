package com.prashant.api_gateway.exception;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public record GatewayErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId
) {
    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JacksonException _) {
            return String.format(
                    "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"traceId\":\"%s\"}",
                    timestamp, status, error, message, path, traceId
            );
        }
    }
}

