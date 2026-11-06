-- V24__make_version_nullable_in_roles_replica.sql
ALTER TABLE restaurant.roles_replica ALTER COLUMN version DROP NOT NULL;

