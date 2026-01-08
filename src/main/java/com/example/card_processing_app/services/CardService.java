package com.example.card_processing_app.services;

import com.example.card_processing_app.dto.request.CreateCardRequestDto;
import com.example.card_processing_app.dto.response.CardResponseDto;
import com.example.card_processing_app.entities.Account;
import com.example.card_processing_app.entities.Card;
import com.example.card_processing_app.entities.User;
import com.example.card_processing_app.enums.CardStatus;
import com.example.card_processing_app.enums.CardType;
import com.example.card_processing_app.enums.CurrencyType;
import com.example.card_processing_app.exception.RecordNotFoundException;
import com.example.card_processing_app.mapper.CardMapper;
import com.example.card_processing_app.repositories.CardRepository;
import com.example.card_processing_app.validations.CommonSchemaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Log4j2
public class CardService {
    private final GeneratorService generatorService;
    private final CardRepository cardRepository;
    private final AccountService accountService;
    private final CommonSchemaValidator commonSchemaValidator;

    @Transactional
    public CardResponseDto createCard(User user, CreateCardRequestDto request) {
        log.info("Processing card creation for user: {}, currency: {}", user.getId(), request.currency());

        Account account = accountService.createAccount(user, request.currency());

        Card card = createCardEntity(account, request.currency());
        Card savedCard = cardRepository.save(card);

        log.info("Card successfully created with ID: {}", savedCard.getId());

        return CardMapper.toDto(savedCard);
    }

    private Card createCardEntity(Account account, CurrencyType currency) {
        CardType type = (currency == CurrencyType.UZS) ? CardType.UZCARD : CardType.VISA;

        Card card = new Card();
        card.setCreatedAt(LocalDateTime.now());
        card.setCardNumber(generatorService.generateCardNumber(type));
        card.setCardType(type);
        card.setStatus(CardStatus.ACTIVE);
        card.setAccount(account);
        card.setCurrency(currency);
        card.setCvv(generatorService.generateCVV());
        card.setExpiryDate(LocalDate.now().plusYears(5).format(DateTimeFormatter.ofPattern("MM/yy")));

        return card;
    }

    @Transactional(readOnly = true)
    public List<CardResponseDto> getAllUserCards(User user) {
        log.info("Getting all cards for user: {}", user.getId());

        List<Card> cards = cardRepository.findAllByUserIdWithAccount(user.getId());
        return cards.stream()
                .map(CardMapper::toDto)
                .toList();
    }

    @Transactional
    public void blockCard(UUID cardId, User user, Long requestVersion) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RecordNotFoundException("Card not found"));

        commonSchemaValidator.validateCardOwnership(card, user);
        commonSchemaValidator.validateOptimisticLock(card, requestVersion);

        if (card.getStatus() == CardStatus.BLOCKED) {
            log.info("Card {} is already blocked", cardId);
            return;
        }

        card.setStatus(CardStatus.BLOCKED);

        cardRepository.save(card);

        log.info("Card {} blocked successfully by user {}", cardId, user.getId());
    }

    @Transactional
    public void unBlockCard(UUID cardId, User user, Long requestVersion) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RecordNotFoundException("Card not found"));

        commonSchemaValidator.validateCardOwnership(card, user);
        commonSchemaValidator.validateOptimisticLock(card, requestVersion);

        if (card.getStatus() == CardStatus.ACTIVE) {
            log.info("Card {} is already active", cardId);
            return;
        }

        card.setStatus(CardStatus.ACTIVE);

        cardRepository.save(card);

        log.info("Card {} unblocked successfully by user {}", cardId, user.getId());
    }

}
