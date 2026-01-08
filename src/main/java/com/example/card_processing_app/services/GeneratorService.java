package com.example.card_processing_app.services;

import com.example.card_processing_app.enums.CardType;
import com.example.card_processing_app.repositories.AccountRepository;
import com.example.card_processing_app.repositories.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GeneratorService {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    public String generateAccountNumber() {
        String number;
        do {
            long randomPart = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
            number = "20208000" + randomPart;
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    public String generateCardNumber(CardType type) {
        String number;
        String bin = (type == CardType.UZCARD) ? "8600" : "4263";
        do {
            long randomPart = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
            number = bin + randomPart;
        } while (cardRepository.existsByCardNumber(number));
        return number;
    }

    public String generateCVV() {
        return String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
