-- V3__add_version_to_user_update_intent.sql

ALTER TABLE user_update_intent ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
