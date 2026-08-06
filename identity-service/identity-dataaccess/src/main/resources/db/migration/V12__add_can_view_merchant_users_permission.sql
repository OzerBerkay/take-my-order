-- V12__add_can_view_merchant_users_permission.sql

-- Insert the permission if it doesn't exist
INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
SELECT gen_random_uuid(), 'can_view_merchant_users', 'Gives merchant user the ability to view users in their restaurant', 'IDENTITY', true, false
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'can_view_merchant_users');

DO $$
DECLARE
    view_merchant_users_perm_id UUID;
BEGIN
    SELECT id INTO view_merchant_users_perm_id FROM permissions WHERE code = 'can_view_merchant_users';

    IF view_merchant_users_perm_id IS NULL THEN
        RETURN;
    END IF;

    -- 1. Remove from MERCHANT_BASE (Cleaning up previous assignment if script was already run partially)
    DELETE FROM role_permissions
    WHERE permission_id = view_merchant_users_perm_id
    AND role_id = (SELECT id FROM roles WHERE name = 'MERCHANT_BASE');

    -- 2. Add to all RESTAURANT_OWNER roles
    INSERT INTO role_permissions (role_id, permission_id)
    SELECT id, view_merchant_users_perm_id
    FROM roles
    WHERE name = 'RESTAURANT_OWNER'
    ON CONFLICT DO NOTHING;

END $$;
