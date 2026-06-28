-- V7__create_password_reset_tokens.sql

CREATE TABLE password_reset_tokens (
                                       id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                       user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       token_hash   VARCHAR(64) NOT NULL,
                                       expires_at   TIMESTAMPTZ NOT NULL,
                                       is_used      BOOLEAN     NOT NULL DEFAULT FALSE,
                                       created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                       CONSTRAINT uq_password_reset_token UNIQUE (token_hash)
);

CREATE INDEX idx_password_reset_user    ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expires ON password_reset_tokens(expires_at) WHERE is_used = FALSE;