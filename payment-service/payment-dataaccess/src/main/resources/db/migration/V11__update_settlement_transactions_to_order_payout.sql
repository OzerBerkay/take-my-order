-- V11__update_settlement_transactions_to_order_payout.sql
-- Update historical delivered order settlement transactions for restaurant wallets from DEPOSIT to ORDER_PAYOUT
UPDATE payment.wallet_transactions wt
SET transaction_type = 'ORDER_PAYOUT'
FROM payment.wallets w
WHERE wt.wallet_id = w.id
  AND w.owner_type = 'RESTAURANT'
  AND wt.transaction_type = 'DEPOSIT'
  AND wt.reference_id IS NOT NULL
  AND wt.reference_id <> 'DIRECT_DEPOSIT';
