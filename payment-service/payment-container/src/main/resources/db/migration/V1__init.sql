CREATE SCHEMA IF NOT EXISTS payment;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status') THEN
        CREATE TYPE payment_status AS ENUM ('COMPLETED', 'CANCELLED', 'FAILED');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS "payment".payments
(
    id uuid NOT NULL,
    customer_id uuid NOT NULL,
    order_id uuid NOT NULL,
    price numeric(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status payment_status NOT NULL,
    CONSTRAINT payments_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "payment".credit_entry
(
    id uuid NOT NULL,
    customer_id uuid NOT NULL,
    total_credit_amount numeric(10,2) NOT NULL,
    CONSTRAINT credit_entry_pkey PRIMARY KEY (id)
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_type') THEN
        CREATE TYPE transaction_type AS ENUM ('DEBIT', 'CREDIT');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS "payment".credit_history
(
    id uuid NOT NULL,
    customer_id uuid NOT NULL,
    amount numeric(10,2) NOT NULL,
    type transaction_type NOT NULL,
    CONSTRAINT credit_history_pkey PRIMARY KEY (id)
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'outbox_status') THEN
        CREATE TYPE outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS "payment".order_outbox
(
    id uuid NOT NULL,
    saga_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    payload jsonb NOT NULL,
    outbox_status outbox_status NOT NULL,
    payment_status payment_status NOT NULL,
    version integer NOT NULL,
    CONSTRAINT order_outbox_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS "payment_order_outbox_saga_status"
    ON "payment".order_outbox
        (type, payment_status);

CREATE UNIQUE INDEX IF NOT EXISTS "payment_order_outbox_saga_id_payment_status_outbox_status"
    ON "payment".order_outbox
        (type, saga_id, payment_status, outbox_status);

-- DATA INSERTION (eski init-data.sql içeriği)
INSERT INTO payment.credit_entry(id, customer_id, total_credit_amount)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb21', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 500.00)
    ON CONFLICT DO NOTHING;

INSERT INTO payment.credit_history(id, customer_id, amount, type)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb23', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 100.00, 'CREDIT')
    ON CONFLICT DO NOTHING;

INSERT INTO payment.credit_history(id, customer_id, amount, type)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb24', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 600.00, 'CREDIT')
    ON CONFLICT DO NOTHING;

INSERT INTO payment.credit_history(id, customer_id, amount, type)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb25', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 200.00, 'DEBIT')
    ON CONFLICT DO NOTHING;

INSERT INTO payment.credit_entry(id, customer_id, total_credit_amount)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb22', 'd215b5f8-0249-4dc5-89a3-51fd148cfb43', 100.00)
    ON CONFLICT DO NOTHING;

INSERT INTO payment.credit_history(id, customer_id, amount, type)
VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb26', 'd215b5f8-0249-4dc5-89a3-51fd148cfb43', 100.00, 'CREDIT')
    ON CONFLICT DO NOTHING;