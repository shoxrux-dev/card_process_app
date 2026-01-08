package com.example.card_processing_app.exception;

public class UserUnauthorizedAction extends RuntimeException{
    public UserUnauthorizedAction(String message) {
        super(message);
    }
}
