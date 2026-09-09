CREATE TABLE tik_tok_user
(
    telegram_id        BIGINT       NOT NULL,
    code_verifier      VARCHAR(255) NOT NULL,
    state              UUID         NOT NULL,
    access_token       VARCHAR(255),
    refresh_token      VARCHAR(255),
    refresh_expires_in INTEGER,
    expires_in         INTEGER,
    open_id            VARCHAR(255),
    scope              VARCHAR(255),
    CONSTRAINT pk_tik_tok_user PRIMARY KEY (telegram_id)
);