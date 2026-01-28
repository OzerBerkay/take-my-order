-- 1. Adım: Kolonu ekle, varsayılan olarak şu anki zamanı ver ve boş olamaz (NOT NULL) de.
-- Bu sayede içerideki tüm kayıtların hepsi bu script çalıştığı anın tarihini alır.
ALTER TABLE IF EXISTS "payment".credit_history
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL;

-- 2. Adım: Veritabanı seviyesindeki "DEFAULT" özelliğini kaldır.
-- Neden? Çünkü biz tarihi Java tarafında (Domain Logic içinde) belirlemek istiyoruz.
-- DB'nin kafasına göre tarih atmasını istemiyoruz. Java null gönderirse DB hata versin istiyoruz.
ALTER TABLE IF EXISTS "payment".credit_history
    ALTER COLUMN created_at DROP DEFAULT;

-- 3. Credit History Pagination Performansı İçin Index
-- customer_id'ye göre filtreleyip, created_at'e göre tersten sıralamak için composite index.
CREATE INDEX IF NOT EXISTS "idx_credit_history_customer_id_created_at"
    ON "payment".credit_history
    (customer_id, created_at DESC);

-- 4. Payment Veri Bütünlüğü İçin Unique Index
-- Bir siparişe (order_id) ait birden fazla ödeme kaydı (payment) oluşmasını engeller.
CREATE UNIQUE INDEX IF NOT EXISTS "uq_payments_order_id"
    ON "payment".payments
    (order_id);