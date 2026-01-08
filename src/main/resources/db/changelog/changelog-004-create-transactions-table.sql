-- liquibase formatted sql
-- changeset developer:004

-- 1. create transactions table
CREATE TABLE transactions (
                              id UUID NOT NULL,
                              created_at TIMESTAMP NOT NULL,

                              card_id UUID NOT NULL,
                              target_card_id UUID NOT NULL,
                              external_id VARCHAR(100),
                              reference_id UUID NOT NULL,

                              amount DECIMAL(19, 2) NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              description TEXT,
                              idempotency_key VARCHAR(255) NOT NULL,

                              before_balance DECIMAL(19, 2) NOT NULL,
                              after_balance DECIMAL(19, 2) NOT NULL,

                              type VARCHAR(20) NOT NULL,
                              currency VARCHAR(3) NOT NULL,
                              purpose VARCHAR(20) NOT NULL,

                              exchange_rate DECIMAL(19, 6),
                              original_amount DECIMAL(19, 2),
                              original_currency VARCHAR(3),
                              failure_reason VARCHAR(500),

                              CONSTRAINT pk_transactions PRIMARY KEY (id, created_at),

                              CONSTRAINT chk_transaction_amount_positive CHECK (amount > 0)
) PARTITION BY RANGE (created_at);

-- add constrain between transaction and card in transactions table
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_card FOREIGN KEY (card_id) REFERENCES cards (id);

-- create indexes
CREATE INDEX idx_transactions_card_at ON transactions(card_id, created_at);
CREATE INDEX idx_transactions_reference_id ON transactions(reference_id);
CREATE INDEX idx_transactions_idempotency_key ON transactions(idempotency_key);
CREATE INDEX idx_transactions_external_id ON transactions(external_id) WHERE external_id IS NOT NULL;

-- create default partition
CREATE TABLE transactions_default PARTITION OF transactions DEFAULT;