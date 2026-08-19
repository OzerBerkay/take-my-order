-- V15__add_can_remove_personnel_permission.sql
INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
VALUES (gen_random_uuid(), 'can_remove_personnel', 'Permission to remove personnel from a restaurant', 'RESTAURANT', true, false)
ON CONFLICT (code) DO NOTHING;
