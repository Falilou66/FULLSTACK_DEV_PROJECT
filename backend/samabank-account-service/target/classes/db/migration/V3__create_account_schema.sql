CREATE SEQUENCE IF NOT EXISTS account_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS accounts (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number  VARCHAR(25)   NOT NULL,
    customer_id     UUID          NOT NULL,
    type            VARCHAR(20)   NOT NULL CHECK (type IN ('CURRENT','SAVINGS')),
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','SUSPENDED','CLOSED')),
    balance         NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    currency        VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    opened_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    version         INTEGER       NOT NULL DEFAULT 0,
    CONSTRAINT uq_account_number    UNIQUE (account_number),
    CONSTRAINT chk_balance_positive CHECK (balance >= 0)
);

CREATE INDEX IF NOT EXISTS idx_accounts_customer ON accounts(customer_id);
CREATE INDEX IF NOT EXISTS idx_accounts_status   ON accounts(status);
CREATE INDEX IF NOT EXISTS idx_accounts_number   ON accounts(account_number);
