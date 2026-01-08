package com.example.card_processing_app.repositories;

import com.example.card_processing_app.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("SELECT t FROM Transaction t WHERE t.card.id = :cardId AND t.card.account.user.id = :userId")
    Page<Transaction> findByCardIdAndUserId(UUID cardId, UUID userId, Pageable pageable);
}
