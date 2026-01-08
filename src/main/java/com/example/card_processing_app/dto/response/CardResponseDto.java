package com.example.card_processing_app.dto.response;

import com.example.card_processing_app.enums.CardType;
import com.example.card_processing_app.enums.CurrencyType;

import java.math.BigDecimal;
import java.util.UUID;

public record CardResponseDto (
        UUID id,
        String cardNumber,
        String expiryDate,
        String cvv,
        String ownerName,
        CardType cardType,
        String accountNumber,
        BigDecimal balance,
        CurrencyType currency,
        String status
) {}
