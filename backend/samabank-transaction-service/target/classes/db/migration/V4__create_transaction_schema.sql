CREATE TABLE IF NOT EXISTS transactions (
    id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    correlation_id     UUID          NOT NULL,
    type               VARCHAR(20)   NOT NULL CHECK (type IN ('DEPOSIT','WITHDRAWAL','TRANSFER')),
    status             VARCHAR(20)   NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('COMPLETED','FAILED','CANCELLED')),
    source_account_id  UUID,
    target_account_id  UUID,
    amount             NUMERIC(15,2) NOT NULL,
    currency           VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    description        VARCHAR(500),
    executed_by        UUID          NOT NULL,
    channel            VARCHAR(20)   NOT NULL DEFAULT 'WEB' CHECK (channel IN ('WEB','BACKOFFICE')),
    executed_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tx_correlation UNIQUE (correlation_id),
    CONSTRAINT chk_tx_amount     CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_tx_source   ON transactions(source_account_id, executed_at DESC);
CREATE INDEX IF NOT EXISTS idx_tx_target   ON transactions(target_account_id, executed_at DESC);
CREATE INDEX IF NOT EXISTS idx_tx_executed ON transactions(executed_at DESC);
CREATE INDEX IF NOT EXISTS idx_tx_status   ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_tx_by       ON transactions(executed_by);
