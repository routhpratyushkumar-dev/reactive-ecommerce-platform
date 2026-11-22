package com.reactivecommerce.common.response;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(boolean success, String errorCode, String message, Map<String, String> validationErrors, Instant timestamp) {
    public static ErrorResponse of(
            String errorCode,
            String message
    ) {
        return new ErrorResponse(
                false,
                errorCode,
                message,
                Map.of(),
                Instant.now()
        );
    }
}
