CREATE INDEX IF NOT EXISTS idx_orders_customer_status ON "order".orders (customer_id, order_status, created_at DESC);
