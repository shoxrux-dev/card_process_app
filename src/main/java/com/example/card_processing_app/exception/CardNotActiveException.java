package com.example.card_processing_app.exception;

public class CardNotActiveException extends RuntimeException {
    public CardNotActiveException(String message) {
        super(message);
    }
}
