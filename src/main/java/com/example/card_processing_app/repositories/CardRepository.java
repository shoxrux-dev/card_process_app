package com.example.card_processing_app.repositories;

import com.example.card_processing_app.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    boolean existsByCardNumber(String cardNumber);

    @Query("SELECT c FROM Card c JOIN FETCH c.account WHERE c.account.user.id= :userId")
    List<Card> findAllByUserIdWithAccount(UUID userId);
}
