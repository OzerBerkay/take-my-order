CREATE SCHEMA IF NOT EXISTS restaurant;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. ENUM TİPLERİ (Şema belirterek çakışmayı önlüyoruz)
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'approval_status' AND n.nspname = 'restaurant'
    ) THEN
CREATE TYPE restaurant.approval_status AS ENUM ('APPROVED', 'REJECTED');
END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'outbox_status' AND n.nspname = 'restaurant'
    ) THEN
CREATE TYPE restaurant.outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');
END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typname = 'order_approval_status' AND n.nspname = 'restaurant'
    ) THEN
CREATE TYPE restaurant.order_approval_status AS ENUM ('APPROVED', 'REJECTED');
END IF;
END $$;

-- 2. RESTAURANTS TABLOSU
CREATE TABLE IF NOT EXISTS restaurant.restaurants
(
    restaurant_id uuid NOT NULL,
    restaurant_name character varying COLLATE pg_catalog."default" NOT NULL,
    is_active boolean NOT NULL,
    CONSTRAINT restaurants_pkey PRIMARY KEY (restaurant_id)
    );

-- 3. PRODUCTS TABLOSU (Direkt Restaurant'a bağlı - One to Many)
CREATE TABLE IF NOT EXISTS restaurant.products
(
    product_id uuid NOT NULL,
    name character varying COLLATE pg_catalog."default" NOT NULL,
    price numeric(10,2) NOT NULL,
    stock integer NOT NULL, -- Stok bilgisini ekledik (Kodda ProductEntity.stock var)
    available boolean NOT NULL,
    restaurant_id uuid NOT NULL, -- İlişki burada kuruluyor
    CONSTRAINT products_pkey PRIMARY KEY (product_id)
    );

-- Foreign Key: Product -> Restaurant
ALTER TABLE restaurant.products
DROP CONSTRAINT IF EXISTS "FK_PRODUCTS_RESTAURANT_ID";

ALTER TABLE restaurant.products
    ADD CONSTRAINT "FK_PRODUCTS_RESTAURANT_ID" FOREIGN KEY (restaurant_id)
        REFERENCES restaurant.restaurants (restaurant_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE; -- Restoran silinirse ürünleri de silinsin

-- 4. ORDER APPROVAL TABLOSU
CREATE TABLE IF NOT EXISTS restaurant.order_approval
(
    id uuid NOT NULL,
    restaurant_id uuid NOT NULL,
    order_id uuid NOT NULL,
    status restaurant.order_approval_status NOT NULL,
    CONSTRAINT order_approval_pkey PRIMARY KEY (id)
    );

-- 5. OUTBOX TABLOSU
CREATE TABLE IF NOT EXISTS restaurant.order_outbox
(
    id uuid NOT NULL,
    saga_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    payload jsonb NOT NULL,
    outbox_status restaurant.outbox_status NOT NULL,
    approval_status restaurant.approval_status NOT NULL,
    version integer NOT NULL,
    CONSTRAINT order_outbox_pkey PRIMARY KEY (id)
    );

-- Indexler
CREATE INDEX IF NOT EXISTS "restaurant_order_outbox_saga_status"
    ON "restaurant".order_outbox
    (type, approval_status);

CREATE UNIQUE INDEX IF NOT EXISTS "restaurant_order_outbox_saga_id"
    ON "restaurant".order_outbox
    (type, saga_id, approval_status, outbox_status);


-- 6. BAŞLANGIÇ VERİLERİ (SEED DATA)
-- Restoranlar
INSERT INTO restaurant.restaurants(restaurant_id, restaurant_name, is_active)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb45', 'Burger King', TRUE) ON CONFLICT DO NOTHING;

INSERT INTO restaurant.restaurants(restaurant_id, restaurant_name, is_active)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb46', 'McDonalds', TRUE) ON CONFLICT DO NOTHING;

-- Ürünler (Burger King için)
INSERT INTO restaurant.products(product_id, name, price, stock, available, restaurant_id)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb47', 'Whopper', 50.00, 100, TRUE, 'd215b5f8-0249-4dc5-89a3-51fd148cfb45') ON CONFLICT DO NOTHING;

INSERT INTO restaurant.products(product_id, name, price, stock, available, restaurant_id)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb48', 'Cola', 15.00, 500, TRUE, 'd215b5f8-0249-4dc5-89a3-51fd148cfb45') ON CONFLICT DO NOTHING;

-- Ürünler (McDonalds için)
INSERT INTO restaurant.products(product_id, name, price, stock, available, restaurant_id)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb49', 'Big Mac', 60.00, 50, TRUE, 'd215b5f8-0249-4dc5-89a3-51fd148cfb46') ON CONFLICT DO NOTHING;