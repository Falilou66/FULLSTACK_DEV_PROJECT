CREATE TABLE users (
                       id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       username         VARCHAR(50)  NOT NULL,
                       email            VARCHAR(255) NOT NULL,
                       password_hash    VARCHAR(255) NOT NULL,
                       role             VARCHAR(20)  NOT NULL
                           CHECK (role IN ('CUSTOMER','TELLER','ADMIN')),
                       status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                           CHECK (status IN ('ACTIVE','LOCKED','SUSPENDED')),
                         -- Remplace SMALLINT par INTEGER
                       failed_attempts  INTEGER      NOT NULL DEFAULT 0,
                       last_login_at    TIMESTAMPTZ,
                       created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at       TIMESTAMcd PTZ  NOT NULL DEFAULT NOW(),
                       version          INTEGER      NOT NULL DEFAULT 0,
                       CONSTRAINT uq_users_username UNIQUE (username),
                       CONSTRAINT uq_users_email    UNIQUE (email)
);

CREATE TABLE refresh_tokens (
                                id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token_hash  VARCHAR(64) NOT NULL,
                                expires_at  TIMESTAMPTZ NOT NULL,
                                is_revoked  BOOLEAN     NOT NULL DEFAULT FALSE,
                                created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                CONSTRAINT uq_refresh_token UNIQUE (token_hash)
);

CREATE INDEX idx_users_username    ON users(username);
CREATE INDEX idx_users_email       ON users(email);
CREATE INDEX idx_users_status      ON users(status);
CREATE INDEX idx_refresh_user      ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_expires   ON refresh_tokens(expires_at)
    WHERE is_revoked = FALSE;