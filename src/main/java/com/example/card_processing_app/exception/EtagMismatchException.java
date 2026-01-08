package com.example.card_processing_app.exception;

public class EtagMismatchException extends RuntimeException {
    public EtagMismatchException(String message) {
        super(message);
    }
}
