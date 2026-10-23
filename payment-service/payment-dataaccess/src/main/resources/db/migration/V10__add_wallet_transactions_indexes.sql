CREATE INDEX IF NOT EXISTS idx_wallet_transactions_wallet_created_at
    ON payment.wallet_transactions (wallet_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_wallet_type_created_at
    ON payment.wallet_transactions (wallet_id, transaction_type, created_at DESC);
