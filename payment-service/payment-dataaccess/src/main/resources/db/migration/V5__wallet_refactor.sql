DROP TABLE IF EXISTS payment.credit_history CASCADE;
DROP TABLE IF EXISTS payment.credit_entry CASCADE;

CREATE TABLE payment.wallets (
    id uuid NOT NULL,
    owner_id uuid NOT NULL,
    owner_type character varying(50) COLLATE pg_catalog."default" NOT NULL,
    balance numeric(10,2) NOT NULL,
    version integer NOT NULL,
    CONSTRAINT wallets_pkey PRIMARY KEY (id),
    CONSTRAINT wallets_owner_id_key UNIQUE (owner_id)
);

CREATE TABLE payment.wallet_transactions (
    id uuid NOT NULL,
    wallet_id uuid NOT NULL,
    amount numeric(10,2) NOT NULL,
    transaction_type character varying(50) COLLATE pg_catalog."default" NOT NULL,
    reference_id character varying(255) COLLATE pg_catalog."default",
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT wallet_transactions_pkey PRIMARY KEY (id),
    CONSTRAINT fk_wallet_id FOREIGN KEY (wallet_id)
        REFERENCES payment.wallets (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);
