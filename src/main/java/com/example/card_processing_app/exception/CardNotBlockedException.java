package com.example.card_processing_app.exception;

public class CardNotBlockedException extends RuntimeException {
    public CardNotBlockedException(String message) {
        super(message);
    }
}
