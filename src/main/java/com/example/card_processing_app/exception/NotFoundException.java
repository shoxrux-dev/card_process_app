package com.example.card_processing_app.exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String massage){
        super(massage);
    }
}
