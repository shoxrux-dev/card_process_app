package com.example.card_processing_app.services;

import com.example.card_processing_app.entities.Account;
import com.example.card_processing_app.entities.User;
import com.example.card_processing_app.enums.AccountStatus;
import com.example.card_processing_app.enums.CurrencyType;
import com.example.card_processing_app.repositories.AccountRepository;
import com.example.card_processing_app.validations.CommonSchemaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final GeneratorService generatorService;

    @Transactional
    public Account createAccount(User user, CurrencyType currency) {
        log.info("Starting account creation for user: {} with currency: {}", user.getId(), currency);

        Optional<Account> byUserAndCurrencyAndStatus = accountRepository.findByUserAndCurrencyAndStatus(user, currency, AccountStatus.ACTIVE);

        if (byUserAndCurrencyAndStatus.isPresent()) {
            return byUserAndCurrencyAndStatus.get();
        }

        Account account = new Account();
        account.setUser(user);
        account.setCreatedAt(LocalDateTime.now());
        account.setCurrency(currency);
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountNumber(generatorService.generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully: {}", savedAccount.getAccountNumber());

        return savedAccount;
    }
}