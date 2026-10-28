-- V12__create_order_reviews_and_add_review_flags.sql

-- 1. Orders tablosuna teslim edilme zamanı ve yorum yapıldı bayrağı ekleme
ALTER TABLE "order".orders 
    ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS is_reviewed BOOLEAN NOT NULL DEFAULT FALSE;

-- Mevcut DELIVERED siparişlerin delivered_at kolonunu created_at ile geriye dönük doldurma (backfill)
UPDATE "order".orders 
SET delivered_at = created_at 
WHERE order_status = 'DELIVERED' AND delivered_at IS NULL;

-- 2. Değerlendirme & Yorum Tablosu
CREATE TABLE IF NOT EXISTS "order".order_reviews (
    id UUID NOT NULL,
    order_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    rating INTEGER NOT NULL,
    comment TEXT,
    customer_name_masked VARCHAR(100) NOT NULL,
    restaurant_reply TEXT,
    restaurant_replied_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivation_reason TEXT,
    deactivated_at TIMESTAMP WITH TIME ZONE,
    deactivated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT order_reviews_pkey PRIMARY KEY (id),
    CONSTRAINT uk_order_reviews_order_id UNIQUE (order_id),
    CONSTRAINT chk_order_reviews_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT fk_order_reviews_order FOREIGN KEY (order_id) REFERENCES "order".orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_reviews_customer FOREIGN KEY (customer_id) REFERENCES "order".customers (id),
    CONSTRAINT fk_order_reviews_restaurant FOREIGN KEY (restaurant_id) REFERENCES "order".restaurant_replica (restaurant_id)
);

-- 3. Performans ve Listeleme İndeksleri
-- Müşteri ve Restoran Genel Listeleme (Aktif yorumlar ve en yeniden eskiye sıralama)
CREATE INDEX IF NOT EXISTS idx_order_reviews_restaurant_active 
    ON "order".order_reviews (restaurant_id, created_at DESC) 
    WHERE is_active = TRUE;

-- Puan ve Yorum Sayısı Rekalkülasyonu için Covering Partial Index (Index-Only Scan)
CREATE INDEX IF NOT EXISTS idx_order_reviews_calc 
    ON "order".order_reviews (restaurant_id, rating) 
    WHERE is_active = TRUE;

-- Admin Moderasyon Filtreleme İndeksi
CREATE INDEX IF NOT EXISTS idx_order_reviews_admin_filter 
    ON "order".order_reviews (restaurant_id, is_active, created_at DESC);

-- Müşterinin kendi yorumlarını listelemesi
CREATE INDEX IF NOT EXISTS idx_order_reviews_customer 
    ON "order".order_reviews (customer_id, created_at DESC);

-- 4. Restaurant Rating Outbox Tablosu
CREATE TABLE IF NOT EXISTS "order".restaurant_rating_outbox (
    id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    outbox_status VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL,
    CONSTRAINT restaurant_rating_outbox_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_rating_outbox_status 
    ON "order".restaurant_rating_outbox (type, outbox_status);

CREATE INDEX IF NOT EXISTS idx_rating_outbox_restaurant_id 
    ON "order".restaurant_rating_outbox (restaurant_id);
