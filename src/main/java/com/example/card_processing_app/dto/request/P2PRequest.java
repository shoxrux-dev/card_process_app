package com.example.card_processing_app.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record P2PRequest(
        @Schema(description = "Sender card ID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        @NotNull(message = "Sender card must be selected")
        UUID senderCardId,

        @Schema(description = "Receiver card ID", example = "b1ffcd88-8d0a-3ff7-aa5c-5cc8ac270b22")
        @NotNull(message = "Receiver card must be provided")
        UUID receiverCardId,

        @Schema(description = "Transfer amount", example = "10.00")
        @NotNull(message = "Amount must be specified")
        @Positive(message = "Amount must be positive")
        @Digits(integer = 19, fraction = 2, message = "Invalid amount format (max 2 decimal places)")
        BigDecimal amount,

        @Schema(description = "External transaction ID from client side or external system",
                example = "EXT-987654321")
        @NotBlank(message = "External ID cannot be empty")
        @Size(max = 50, message = "External ID is too long (max 50 characters)")
        String externalId,

        @Schema(description = "Transaction purpose or comment", example = "For lunch")
        @Size(max = 100, message = "Comment is too long (max 100 characters)")
        String description
) implements Serializable {

    @Schema(hidden = true)
    @JsonIgnore
    @AssertTrue(message = "Sender and receiver cards must be different")
    public boolean isSenderAndReceiverDifferent() {
        if (senderCardId == null || receiverCardId == null) {
            return true;
        }
        return !senderCardId.equals(receiverCardId);
    }
}