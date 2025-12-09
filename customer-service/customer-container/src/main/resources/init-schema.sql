-- 1. ŞEMA KURULUMU
DROP SCHEMA IF EXISTS customer CASCADE;

CREATE SCHEMA customer;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS customer.customers CASCADE;

CREATE TABLE customer.customers
(
    id uuid NOT NULL,
    username character varying COLLATE pg_catalog."default" NOT NULL,
    first_name character varying COLLATE pg_catalog."default" NOT NULL,
    last_name character varying COLLATE pg_catalog."default" NOT NULL,
    email character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT customers_pkey PRIMARY KEY (id)
);

-- 2. Outbox Status Enum
DROP TYPE IF EXISTS outbox_status;
CREATE TYPE outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');

-- 3. Outbox Tablosu
-- Kafka'ya gidecek mesajlar burada güvenle saklanacak.
CREATE TABLE customer.customer_outbox
(
    id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    payload jsonb NOT NULL,
    outbox_status outbox_status NOT NULL,
    version integer NOT NULL,
    CONSTRAINT customer_outbox_pkey PRIMARY KEY (id)
);

-- 4. İndeksler
CREATE INDEX "customer_outbox_status"
    ON customer.customer_outbox
        (type, outbox_status);

-- Kayıt esnasında aynı kullanıcı adından iki kişi olmasın,
-- bunun için username arama yapıldığında sistem ışık hızında çalışsın diye.
CREATE UNIQUE INDEX "customers_username_index"
    ON customer.customers
        (username);

CREATE UNIQUE INDEX "customers_email_index"
    ON customer.customers
        (email);
