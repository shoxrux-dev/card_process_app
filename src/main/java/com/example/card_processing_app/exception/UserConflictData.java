package com.example.card_processing_app.exception;

public class UserConflictData extends RuntimeException{
    public UserConflictData(String message) {
        super(message);
    }
}
