CREATE SCHEMA IF NOT EXISTS restaurant;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS restaurant.restaurants
(
    id uuid NOT NULL,
    name character varying COLLATE pg_catalog."default" NOT NULL,
    active boolean NOT NULL,
    CONSTRAINT restaurants_pkey PRIMARY KEY (id)
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'approval_status') THEN
        CREATE TYPE approval_status AS ENUM ('APPROVED', 'REJECTED');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS restaurant.order_approval
(
    id uuid NOT NULL,
    restaurant_id uuid NOT NULL,
    order_id uuid NOT NULL,
    status approval_status NOT NULL,
    CONSTRAINT order_approval_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS restaurant.products
(
    id uuid NOT NULL,
    name character varying COLLATE pg_catalog."default" NOT NULL,
    price numeric(10,2) NOT NULL,
    available boolean NOT NULL,
    CONSTRAINT products_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS restaurant.restaurant_products
(
    id uuid NOT NULL,
    restaurant_id uuid NOT NULL,
    product_id uuid NOT NULL,
    CONSTRAINT restaurant_products_pkey PRIMARY KEY (id)
);

ALTER TABLE restaurant.restaurant_products
    DROP CONSTRAINT IF EXISTS "FK_RESTAURANT_ID";

ALTER TABLE restaurant.restaurant_products
    ADD CONSTRAINT "FK_RESTAURANT_ID" FOREIGN KEY (restaurant_id)
        REFERENCES restaurant.restaurants (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE RESTRICT;

ALTER TABLE restaurant.restaurant_products
    DROP CONSTRAINT IF EXISTS "FK_PRODUCT_ID";

ALTER TABLE restaurant.restaurant_products
    ADD CONSTRAINT "FK_PRODUCT_ID" FOREIGN KEY (product_id)
        REFERENCES restaurant.products (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE RESTRICT;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'outbox_status') THEN
        CREATE TYPE outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS restaurant.order_outbox
(
    id uuid NOT NULL,
    saga_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    payload jsonb NOT NULL,
    outbox_status outbox_status NOT NULL,
    approval_status approval_status NOT NULL,
    version integer NOT NULL,
    CONSTRAINT order_outbox_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS "restaurant_order_outbox_saga_status"
    ON "restaurant".order_outbox
        (type, approval_status);

CREATE UNIQUE INDEX IF NOT EXISTS "restaurant_order_outbox_saga_id"
    ON "restaurant".order_outbox
        (type, saga_id, approval_status, outbox_status);

DROP MATERIALIZED VIEW IF EXISTS restaurant.order_restaurant_m_view;

CREATE MATERIALIZED VIEW restaurant.order_restaurant_m_view
TABLESPACE pg_default
AS
SELECT r.id AS restaurant_id,
       r.name AS restaurant_name,
       r.active AS restaurant_active,
       p.id AS product_id,
       p.name AS product_name,
       p.price AS product_price,
       p.available AS product_available
FROM restaurant.restaurants r,
     restaurant.products p,
     restaurant.restaurant_products rp
WHERE r.id = rp.restaurant_id AND p.id = rp.product_id
    WITH DATA;

refresh materialized VIEW restaurant.order_restaurant_m_view;

CREATE OR replace function restaurant.refresh_order_restaurant_m_view()
returns trigger
AS '
BEGIN
    refresh materialized VIEW restaurant.order_restaurant_m_view;
    return null;
END;
'  LANGUAGE plpgsql;

DROP trigger IF EXISTS refresh_order_restaurant_m_view ON restaurant.restaurant_products;

CREATE trigger refresh_order_restaurant_m_view
    after INSERT OR UPDATE OR DELETE OR truncate
                    ON restaurant.restaurant_products FOR each statement
                        EXECUTE PROCEDURE restaurant.refresh_order_restaurant_m_view();


-- DATA INSERTION (old init-data.sql)
INSERT INTO restaurant.restaurants(id, name, active)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb45', 'restaurant_1', TRUE) ON CONFLICT DO NOTHING;
INSERT INTO restaurant.restaurants(id, name, active)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb46', 'restaurant_2', FALSE) ON CONFLICT DO NOTHING;

INSERT INTO restaurant.products(id, name, price, available)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb47', 'product_1', 25.00, FALSE) ON CONFLICT DO NOTHING;
INSERT INTO restaurant.products(id, name, price, available)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb48', 'product_2', 50.00, TRUE) ON CONFLICT DO NOTHING;
INSERT INTO restaurant.products(id, name, price, available)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb49', 'product_3', 20.00, FALSE) ON CONFLICT DO NOTHING;
INSERT INTO restaurant.products(id, name, price, available)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb50', 'product_4', 40.00, TRUE) ON CONFLICT DO NOTHING;

INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb51', 'd215b5f8-0249-4dc5-89a3-51fd148cfb45', 'd215b5f8-0249-4dc5-89a3-51fd148cfb47') ON CONFLICT DO NOTHING;
INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb52', 'd215b5f8-0249-4dc5-89a3-51fd148cfb45', 'd215b5f8-0249-4dc5-89a3-51fd148cfb48') ON CONFLICT DO NOTHING;
INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb53', 'd215b5f8-0249-4dc5-89a3-51fd148cfb46', 'd215b5f8-0249-4dc5-89a3-51fd148cfb49') ON CONFLICT DO NOTHING;
INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb54', 'd215b5f8-0249-4dc5-89a3-51fd148cfb46', 'd215b5f8-0249-4dc5-89a3-51fd148cfb50') ON CONFLICT DO NOTHING;