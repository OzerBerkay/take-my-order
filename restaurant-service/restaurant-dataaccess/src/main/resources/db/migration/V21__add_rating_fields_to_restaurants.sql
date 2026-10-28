-- V21__add_rating_fields_to_restaurants.sql
ALTER TABLE restaurant.restaurants 
    ADD COLUMN IF NOT EXISTS average_rating NUMERIC(2,1) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS total_reviews INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS rating_updated_at TIMESTAMP WITH TIME ZONE DEFAULT NULL;

-- Restoran listelemede puana ve yorum sayısına göre sıralama performans indeksi
CREATE INDEX IF NOT EXISTS idx_restaurants_rating 
    ON restaurant.restaurants (average_rating DESC NULLS LAST, total_reviews DESC);
