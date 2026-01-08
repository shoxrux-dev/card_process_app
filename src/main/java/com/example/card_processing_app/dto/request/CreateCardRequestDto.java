package com.example.card_processing_app.dto.request;

import com.example.card_processing_app.enums.CurrencyType;
import jakarta.validation.constraints.NotNull;

public record CreateCardRequestDto (
        @NotNull CurrencyType currency
) {}
