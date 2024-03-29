-- changeset severett:202402041200
CREATE TABLE IF NOT EXISTS account
(
    id     VARCHAR(255) PRIMARY KEY,
    amount DECIMAL NOT NULL DEFAULT 0.0
);

CREATE TYPE TRANSACTION_TYPE AS ENUM ('DEPOSIT', 'WITHDRAWAL');

CREATE TABLE IF NOT EXISTS transaction
(
    id            BIGSERIAL PRIMARY KEY,
    account_id    VARCHAR(255)     NOT NULL REFERENCES account (id) ON DELETE CASCADE,
    type          TRANSACTION_TYPE NOT NULL,
    serial_number BIGINT           NOT NULL,
    amount        DECIMAL          NOT NULL,
    timestamp     TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    UNIQUE (account_id, serial_number)
);
