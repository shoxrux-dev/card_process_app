package com.example.card_processing_app.validations;

import com.example.card_processing_app.entities.Card;
import com.example.card_processing_app.entities.User;
import com.example.card_processing_app.exception.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
public class CommonSchemaValidator {
    public void validateCardOwnership(Card card, User user) {
        if (card == null || user == null) {
            throw new IllegalArgumentException("Card or User cannot be null");
        }

        UUID ownerId = null;
        if (card.getAccount() != null && card.getAccount().getUser() != null) {
            ownerId = card.getAccount().getUser().getId();
        }

        if (ownerId == null || !ownerId.equals(user.getId())) {
            throw new AccessDeniedException("Ownership validation failed: You can only access your own cards");
        }
    }

    public void validateOptimisticLock(Card card, Long version) {
        if (!card.getVersion().equals(version)) {
            throw new OptimisticLockException("Card state has changed. Please refresh and try again.");
        }
    }
}
