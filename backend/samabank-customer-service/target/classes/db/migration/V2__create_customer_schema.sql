CREATE SEQUENCE IF NOT EXISTS customer_number_seq
    START WITH 1000
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS customers (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL,
    customer_number VARCHAR(20)  NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    date_of_birth   DATE         NOT NULL,
    email           VARCHAR(255) NOT NULL,
    phone           VARCHAR(20),
    address         VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE','SUSPENDED','CLOSED')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_customers_user   UNIQUE (user_id),
    CONSTRAINT uq_customers_number UNIQUE (customer_number)
);

CREATE INDEX IF NOT EXISTS idx_customers_status ON customers(status);
CREATE INDEX IF NOT EXISTS idx_customers_user   ON customers(user_id);
