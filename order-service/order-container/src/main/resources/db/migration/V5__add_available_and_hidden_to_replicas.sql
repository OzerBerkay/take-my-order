ALTER TABLE "order".restaurant_replica
    ADD COLUMN available boolean NOT NULL DEFAULT true;

ALTER TABLE "order".product_replica
    ADD COLUMN hidden boolean NOT NULL DEFAULT false;
