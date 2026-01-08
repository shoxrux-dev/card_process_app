package com.example.card_processing_app.dto.response;

import com.example.card_processing_app.enums.CurrencyType;
import com.example.card_processing_app.enums.PurposeType;
import com.example.card_processing_app.enums.TransactionStatus;
import com.example.card_processing_app.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDto(
        UUID transactionId,
        String externalId,
        UUID cardId,
        BigDecimal afterBalance,
        BigDecimal amount,
        CurrencyType currency,
        PurposeType purpose,
        TransactionType type,
        TransactionStatus status,
        LocalDateTime createdAt,
        String description
) {}
