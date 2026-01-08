package com.example.card_processing_app.mapper;

import com.example.card_processing_app.dto.response.TransactionResponseDto;
import com.example.card_processing_app.entities.Transaction;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TransactionMapper {
    public static TransactionResponseDto toDto(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getId(),
                transaction.getExternalId(),
                transaction.getCard() != null ? transaction.getCard().getId() : null,
                transaction.getAfterBalance(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getPurpose(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getDescription()
        );
    }
}
