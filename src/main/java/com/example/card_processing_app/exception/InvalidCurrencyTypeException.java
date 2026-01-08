package com.example.card_processing_app.exception;

public class InvalidCurrencyTypeException extends RuntimeException {
    public InvalidCurrencyTypeException(String message) {
        super(message);
    }
}
