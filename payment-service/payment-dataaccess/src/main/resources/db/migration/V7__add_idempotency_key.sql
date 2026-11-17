ALTER TABLE payment.wallet_transactions
ADD COLUMN idempotency_key character varying(255);

ALTER TABLE payment.wallet_transactions
ADD CONSTRAINT wallet_transactions_idempotency_key_key UNIQUE (idempotency_key);
