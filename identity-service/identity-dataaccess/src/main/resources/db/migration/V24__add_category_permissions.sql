-- V24__add_category_permissions.sql

-- 1. Insert New Permission for Category Management
INSERT INTO permissions (id, code, description, domain, active, is_restricted) VALUES
('3a2f9b8c-5a9d-48d6-84d4-29c489c629ab', 'can_manage_category', 'can_manage_category', 'RESTAURANT', true, false);

-- 2. Assign Permission to all existing RESTAURANT_OWNER roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT id, '3a2f9b8c-5a9d-48d6-84d4-29c489c629ab'
FROM roles
WHERE name = 'RESTAURANT_OWNER';
