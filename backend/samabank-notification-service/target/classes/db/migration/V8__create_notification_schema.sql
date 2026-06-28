CREATE TABLE IF NOT EXISTS notifications (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_email VARCHAR(255) NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    type            VARCHAR(50)  NOT NULL CHECK (type IN (
        'WELCOME','DEPOSIT_CONFIRMATION','WITHDRAWAL_CONFIRMATION','TRANSFER_CONFIRMATION',
        'ACCOUNT_OPENED','ACCOUNT_SUSPENDED','LOGIN_ALERT','ACCOUNT_LOCKED','PASSWORD_RESET'
    )),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','SENT','FAILED')),
    error_message   VARCHAR(500),
    sent_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_type   ON notifications(type, created_at DESC);
