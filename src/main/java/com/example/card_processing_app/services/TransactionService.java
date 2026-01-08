package com.example.card_processing_app.services;

import com.example.card_processing_app.dto.request.P2PRequest;
import com.example.card_processing_app.dto.response.TransactionResponseDto;
import com.example.card_processing_app.entities.Account;
import com.example.card_processing_app.entities.Card;
import com.example.card_processing_app.entities.Transaction;
import com.example.card_processing_app.entities.User;
import com.example.card_processing_app.enums.TransactionStatus;
import com.example.card_processing_app.enums.TransactionType;
import com.example.card_processing_app.exception.InsufficientFundsException;
import com.example.card_processing_app.exception.RecordNotFoundException;
import com.example.card_processing_app.mapper.TransactionMapper;
import com.example.card_processing_app.repositories.AccountRepository;
import com.example.card_processing_app.repositories.CardRepository;
import com.example.card_processing_app.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CbuService cbuService;

    @Transactional
    public void executeP2P(P2PRequest request, String idempotencyKey) {
        Card senderCard = cardRepository.findById(request.senderCardId())
                .orElseThrow(() -> new RecordNotFoundException("Sender card not found"));
        Card receiverCard = cardRepository.findById(request.receiverCardId())
                .orElseThrow(() -> new RecordNotFoundException("Receiver card not found"));

        UUID senderAccId = senderCard.getAccount().getId();
        UUID receiverAccId = receiverCard.getAccount().getId();
        UUID refId = UUID.randomUUID();

        List<Transaction> transactions = prepareInitialTransactions(senderCard, receiverCard, request, idempotencyKey, refId);

        try {
            BigDecimal exchangeRate = cbuService.getExchangeRate(senderCard.getCurrency(), receiverCard.getCurrency());
            BigDecimal amountInSenderCurrency = request.amount();
            BigDecimal amountInReceiverCurrency = request.amount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);

            if (senderAccId.equals(receiverAccId)) {
                handleInternalTransaction(senderAccId, amountInSenderCurrency, amountInReceiverCurrency, transactions);
            } else {
                handleExternalTransaction(senderAccId, receiverAccId, amountInSenderCurrency, amountInReceiverCurrency, transactions);
            }

            updateTransactionStatus(transactions, TransactionStatus.COMPLETED);

        } catch (Exception e) {
            updateTransactionStatus(transactions, TransactionStatus.FAILED);
            throw e;
        }
    }

    private void handleExternalTransaction(UUID sAccId, UUID rAccId, BigDecimal sAmount, BigDecimal rAmount, List<Transaction> txs) {
        UUID firstId = sAccId.compareTo(rAccId) < 0 ? sAccId : rAccId;
        UUID secondId = firstId.equals(sAccId) ? rAccId : sAccId;

        Account firstAcc = accountRepository.findByIdWithLock(firstId).orElseThrow(() -> new RecordNotFoundException("Account not found"));
        Account secondAcc = accountRepository.findByIdWithLock(secondId).orElseThrow(() -> new RecordNotFoundException("Account not found"));

        Account senderAcc = firstAcc.getId().equals(sAccId) ? firstAcc : secondAcc;
        Account receiverAcc = firstAcc.getId().equals(rAccId) ? firstAcc : secondAcc;

        if (senderAcc.getBalance().compareTo(sAmount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on sender account");
        }

        BigDecimal sBefore = senderAcc.getBalance();
        BigDecimal rBefore = receiverAcc.getBalance();

        senderAcc.setBalance(sBefore.subtract(sAmount));
        receiverAcc.setBalance(rBefore.add(rAmount));

        finalizeTransactionBalances(txs, sBefore, rBefore, sAmount, rAmount);
    }

    private void handleInternalTransaction(UUID accId, BigDecimal sAmount, BigDecimal rAmount, List<Transaction> txs) {
        Account acc = accountRepository.findByIdWithLock(accId).orElseThrow(() -> new RecordNotFoundException("Account not found"));

        if (acc.getBalance().compareTo(sAmount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        BigDecimal before = acc.getBalance();
        acc.setBalance(before.subtract(sAmount).add(rAmount));

        finalizeTransactionBalances(txs, before, before.subtract(sAmount), sAmount, rAmount);
    }

    private List<Transaction> prepareInitialTransactions(Card sender, Card receiver, P2PRequest req, String idKey, UUID refId) {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                createBaseTransaction(sender, receiver.getId(), req.amount(), TransactionType.DEBIT, idKey, refId, now, req),
                createBaseTransaction(receiver, sender.getId(), req.amount(), TransactionType.CREDIT, idKey + "-CR", refId, now, req)
        );
    }

    private Transaction createBaseTransaction(Card card, UUID target, BigDecimal amount, TransactionType type, String idKey, UUID refId, LocalDateTime now, P2PRequest req) {
        Transaction tx = new Transaction();
        tx.setCard(card);
        tx.setTargetCardId(target);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setReferenceId(refId);
        tx.setIdempotencyKey(idKey);
        tx.setExternalId(req.externalId());
        tx.setDescription(req.description());
        tx.setCurrency(card.getCurrency());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setCreatedAt(now);
        return tx;
    }

    private void finalizeTransactionBalances(List<Transaction> txs, BigDecimal sBefore, BigDecimal rBefore, BigDecimal sAmount, BigDecimal rAmount) {
        for (Transaction tx : txs) {
            if (tx.getType() == TransactionType.DEBIT) {
                tx.setBeforeBalance(sBefore);
                tx.setAfterBalance(sBefore.subtract(sAmount));
                tx.setAmount(sAmount);
            } else {
                tx.setBeforeBalance(rBefore);
                tx.setAfterBalance(rBefore.add(rAmount));
                tx.setAmount(rAmount);
            }
        }
    }

    private void updateTransactionStatus(List<Transaction> txs, TransactionStatus status) {
        txs.forEach(tx -> tx.setStatus(status));
        transactionRepository.saveAll(txs);
    }

    public Page<TransactionResponseDto> getTransactionHistoryByCardId(UUID cardId, Pageable pageable, User user) {
        return transactionRepository.findByCardIdAndUserId(cardId, user.getId(), pageable)
                .map(TransactionMapper::toDto);
    }
}