ALTER TABLE "order".restaurant_replica
ADD COLUMN minimum_order_amount numeric(10, 2),
ADD COLUMN delivery_fee numeric(10, 2);
