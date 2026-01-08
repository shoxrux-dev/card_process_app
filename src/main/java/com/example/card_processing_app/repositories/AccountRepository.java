package com.example.card_processing_app.repositories;

import com.example.card_processing_app.entities.Account;
import com.example.card_processing_app.entities.User;
import com.example.card_processing_app.enums.AccountStatus;
import com.example.card_processing_app.enums.CurrencyType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByUserAndCurrencyAndStatus(User user, CurrencyType currency, AccountStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance - :amount WHERE a.id = :id AND a.balance >= :amount")
    int decreaseBalance(@Param("id") UUID id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.id = :id")
    int increaseBalance(@Param("id") UUID id, @Param("amount") BigDecimal amount);
}
