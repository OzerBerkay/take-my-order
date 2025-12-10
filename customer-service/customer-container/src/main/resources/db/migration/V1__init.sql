-- 1. ŞEMA KURULUMU
CREATE SCHEMA IF NOT EXISTS customer;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS customer.customers
(
    id uuid NOT NULL,
    username character varying COLLATE pg_catalog."default" NOT NULL,
    first_name character varying COLLATE pg_catalog."default" NOT NULL,
    last_name character varying COLLATE pg_catalog."default" NOT NULL,
    email character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT customers_pkey PRIMARY KEY (id)
);

-- 2. Outbox Status Enum
-- Do end bloğu ile sarıldı çünkü diğer türlü ilk çalıştırdığında harika çalışır.
-- Fakat ikinci kez çalıştığında (veya veritabanında bu tip zaten varsa) PostgreSQL şu
-- hatayı fırlatır ve işlemi durdurur: ERROR: type "outbox_status" already exists
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'outbox_status') THEN
        CREATE TYPE outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');
    END IF;
END $$;

-- 3. Outbox Tablosu
-- Kafka'ya gidecek mesajlar burada güvenle saklanacak.
CREATE TABLE IF NOT EXISTS customer.customer_outbox
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
CREATE INDEX IF NOT EXISTS "customer_outbox_status"
    ON customer.customer_outbox
        (type, outbox_status);

-- Kayıt esnasında aynı kullanıcı adından iki kişi olmasın,
-- bunun için username arama yapıldığında sistem ışık hızında çalışsın diye.
CREATE UNIQUE INDEX IF NOT EXISTS "customers_username_index"
    ON customer.customers
        (username);

CREATE UNIQUE INDEX IF NOT EXISTS "customers_email_index"
    ON customer.customers
        (email);
