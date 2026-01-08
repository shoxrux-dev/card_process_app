package com.example.card_processing_app.dto;

import com.example.card_processing_app.enums.IdempotencyStatus;

public record IdempotencyResult(
        IdempotencyStatus status,
        String cachedValue
) {
    public static IdempotencyResult newRequest() {
        return new IdempotencyResult(IdempotencyStatus.NEW, null);
    }

    public static IdempotencyResult processing() {
        return new IdempotencyResult(IdempotencyStatus.PROCESSING, null);
    }

    public static IdempotencyResult completed(String value) {
        return new IdempotencyResult(IdempotencyStatus.COMPLETED, value);
    }
}
