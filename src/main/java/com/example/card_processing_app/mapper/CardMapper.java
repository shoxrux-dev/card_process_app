package com.example.card_processing_app.mapper;

import com.example.card_processing_app.dto.response.CardResponseDto;
import com.example.card_processing_app.entities.Card;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CardMapper {
    public CardResponseDto toDto(Card card) {
        return new CardResponseDto(
                card.getId(),
                card.getCardNumber(),
                card.getExpiryDate(),
                card.getCvv(),
                card.getAccount().getUser().getUsername(),
                card.getCardType(),
                card.getAccount().getAccountNumber(),
                card.getAccount().getBalance(),
                card.getAccount().getCurrency(),
                card.getStatus().name()
        );
    }
}
