package com.example.card_processing_app.exception;

public class IdempotentRequestException extends RuntimeException {
    public IdempotentRequestException(String message) {
        super(message);
    }
}
