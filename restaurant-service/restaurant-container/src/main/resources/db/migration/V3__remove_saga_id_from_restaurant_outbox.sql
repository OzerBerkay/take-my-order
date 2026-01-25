-- Önce eski index'i siliyoruz. Çünkü bu index 'saga_id' kolonunu kullanıyordu, ismi de 'saga_status' idi, artık anlamsız.
DROP INDEX IF EXISTS restaurant.restaurant_outbox_saga_status;

-- Artık gereksiz olan 'saga_id' kolonunu tablodan uçuruyoruz.
ALTER TABLE restaurant.restaurant_outbox
DROP COLUMN saga_id;

-- Scheduler sorgusu için (findByTypeAndOutboxStatus) yeni ve temiz bir index oluşturuyoruz.
-- Artık sadece type ve outbox_status'a bakıyor.
CREATE INDEX "restaurant_outbox_scheduler_idx"
    ON restaurant.restaurant_outbox
        (type, outbox_status);