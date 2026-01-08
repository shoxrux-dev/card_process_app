package com.example.card_processing_app.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String massage) {
        super(massage);
    }
}

