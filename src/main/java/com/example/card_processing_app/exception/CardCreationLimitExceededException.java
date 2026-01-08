package com.example.card_processing_app.exception;

public class CardCreationLimitExceededException extends RuntimeException {
    public CardCreationLimitExceededException(String message) {
        super(message);
    }
}
