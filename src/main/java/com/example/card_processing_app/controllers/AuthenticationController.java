package com.example.card_processing_app.controllers;

import com.example.card_processing_app.dto.request.LoginRequestDto;
import com.example.card_processing_app.dto.request.RegisterRequestDto;
import com.example.card_processing_app.dto.response.UserResponseDto;
import com.example.card_processing_app.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody RegisterRequestDto request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

}
