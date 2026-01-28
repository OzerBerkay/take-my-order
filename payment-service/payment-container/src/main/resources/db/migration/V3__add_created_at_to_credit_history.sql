-- 1. Adım: Kolonu ekle, varsayılan olarak şu anki zamanı ver ve boş olamaz (NOT NULL) de.
-- Bu sayede içeride 1 milyon kayıt varsa hepsi bu script çalıştığı anın tarihini alır.
ALTER TABLE credit_history
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL;

-- 2. Adım: Veritabanı seviyesindeki "DEFAULT" özelliğini kaldır.
-- Neden? Çünkü biz tarihi Java tarafında (Domain Logic içinde) belirlemek istiyoruz.
-- DB'nin kafasına göre tarih atmasını istemiyoruz. Java null gönderirse DB hata versin istiyoruz.
ALTER TABLE credit_history
    ALTER COLUMN created_at DROP DEFAULT;