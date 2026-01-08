package com.example.card_processing_app.mapper;

import com.example.card_processing_app.dto.response.UserResponseDto;
import com.example.card_processing_app.entities.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {
    public UserResponseDto toDto(String token, User user) {
        return new UserResponseDto(
                user.getEmail(),
                token
        );
    }
}
