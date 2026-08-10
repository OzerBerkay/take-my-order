-- Veritabanı docker-compose'da POSTGRES_DB=takemyorder olarak zaten oluşuyor.
-- Sadece schema'ları oluşturmamız yeterli.

CREATE SCHEMA IF NOT EXISTS "order";
CREATE SCHEMA IF NOT EXISTS payment;
CREATE SCHEMA IF NOT EXISTS restaurant;
CREATE SCHEMA IF NOT EXISTS customer;
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS keycloak;
