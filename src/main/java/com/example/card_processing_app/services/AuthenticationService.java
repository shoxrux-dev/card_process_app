package com.example.card_processing_app.services;

import com.example.card_processing_app.config.token.JwtService;
import com.example.card_processing_app.dto.request.LoginRequestDto;
import com.example.card_processing_app.dto.request.RegisterRequestDto;
import com.example.card_processing_app.dto.response.UserResponseDto;
import com.example.card_processing_app.exception.AlreadyExistsException;
import com.example.card_processing_app.exception.NotFoundException;
import com.example.card_processing_app.exception.RecordNotFoundException;
import com.example.card_processing_app.mapper.UserMapper;
import com.example.card_processing_app.entities.User;
import com.example.card_processing_app.repositories.RoleRepository;
import com.example.card_processing_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserResponseDto register(RegisterRequestDto dto) {
        if (repository.existsByEmail(dto.email())) {
            log.warn("Registration failed — email already registered: {}", dto.email());
            throw new AlreadyExistsException("Email already registered: " + dto.email());
        }

        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RecordNotFoundException("Default Role USER not found"));

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRoles(Set.of(userRole));

        User savedUser = repository.save(user);
        log.info("User successfully registered: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        String token = jwtService.generateToken(savedUser);
        return UserMapper.toDto(token, savedUser);
    }

    public UserResponseDto authenticate(LoginRequestDto dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        User userDB = repository.findByEmail(dto.email())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String token = jwtService.generateToken(userDB);
        log.info("Authentication successful for user: {}", dto.email());

        return UserMapper.toDto(token, userDB);
    }
}