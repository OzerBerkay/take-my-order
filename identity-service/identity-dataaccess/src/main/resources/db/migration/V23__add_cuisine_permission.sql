-- V23__add_cuisine_permission.sql

-- 1. Insert New Permissions for Cuisine Management
INSERT INTO permissions (id, code, description, domain, active, is_restricted) VALUES
('590f9492-64d0-41d4-a4e0-14bf5c45d950', 'can_create_cuisine_type', 'can_create_cuisine_type', 'RESTAURANT', true, false),
('9ea10a0f-9482-4c40-94ee-d203e1133570', 'can_update_cuisine_type', 'can_update_cuisine_type', 'RESTAURANT', true, false),
('70bcd390-f4b2-41cc-90bc-5854e64dfefe', 'can_delete_cuisine_type', 'can_delete_cuisine_type', 'RESTAURANT', true, false),
('51526b4b-cd05-4396-a981-8f87df63adcb', 'can_read_cuisine_type', 'can_read_cuisine_type', 'RESTAURANT', true, false);

-- 2. Assign Permissions to SYSTEM_ADMIN Role (id: 019fb322-1617-74e0-b073-93b0948ea0dc)
INSERT INTO role_permissions (role_id, permission_id) VALUES
('019fb322-1617-74e0-b073-93b0948ea0dc', '590f9492-64d0-41d4-a4e0-14bf5c45d950'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '9ea10a0f-9482-4c40-94ee-d203e1133570'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '70bcd390-f4b2-41cc-90bc-5854e64dfefe'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '51526b4b-cd05-4396-a981-8f87df63adcb');

