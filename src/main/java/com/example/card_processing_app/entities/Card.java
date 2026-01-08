package com.example.card_processing_app.entities;

import com.example.card_processing_app.enums.CardStatus;
import com.example.card_processing_app.enums.CardType;
import com.example.card_processing_app.enums.CurrencyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cards")
public class Card extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(unique = true, nullable = false, length = 16)
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyType currency;

    @Column(nullable = false, length = 5)
    private String expiryDate;

    @Column(nullable = false, length = 3)
    private String cvv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Version
    private Long version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return id != null && id.equals(card.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
