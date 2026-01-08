-- liquibase formatted sql
-- changeset developer:003

-- create accounts table
CREATE TABLE accounts (
                        id UUID PRIMARY KEY,
                        account_number VARCHAR(20) NOT NULL UNIQUE,
                        balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
                        currency VARCHAR(10) NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        user_id UUID NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP,
                        CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- create cards table
CREATE TABLE cards (
                       id UUID PRIMARY KEY,
                       card_number VARCHAR(16) NOT NULL UNIQUE,
                       expiry_date VARCHAR(5) NOT NULL,
                       cvv VARCHAR(3) NOT NULL,
                       currency VARCHAR(10) NOT NULL,
                       card_type VARCHAR(20) NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       account_id UUID NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP,
                       version BIGINT DEFAULT 0 NOT NULL,
                       CONSTRAINT fk_card_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- create indexes
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_cards_account_id ON cards(account_id);
CREATE INDEX idx_cards_card_number ON cards(card_number);