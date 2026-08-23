-- V19__add_order_management_permissions.sql

-- 1. Insert 'can_manage_order' and 'can_read_order' permissions if they do not exist
-- Both are restricted=false so that merchants can assign them to other personnel.
INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
SELECT gen_random_uuid(), 'can_manage_order', 'Gives ability to approve or reject incoming orders', 'ORDER', true, false
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'can_manage_order');

INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
SELECT gen_random_uuid(), 'can_read_order', 'Gives ability to view incoming active orders', 'ORDER', true, false
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'can_read_order');

DO $$
DECLARE
    manage_order_perm_id UUID;
    read_order_perm_id UUID;
BEGIN
    SELECT id INTO manage_order_perm_id FROM permissions WHERE code = 'can_manage_order';
    SELECT id INTO read_order_perm_id FROM permissions WHERE code = 'can_read_order';

    IF manage_order_perm_id IS NULL OR read_order_perm_id IS NULL THEN
        RETURN;
    END IF;

    -- 2. Add permissions to all RESTAURANT_OWNER roles
    INSERT INTO role_permissions (role_id, permission_id)
    SELECT id, manage_order_perm_id
    FROM roles
    WHERE name = 'RESTAURANT_OWNER'
    ON CONFLICT DO NOTHING;

    INSERT INTO role_permissions (role_id, permission_id)
    SELECT id, read_order_perm_id
    FROM roles
    WHERE name = 'RESTAURANT_OWNER'
    ON CONFLICT DO NOTHING;

    -- 3. Add permissions to SYSTEM_ADMIN role
    INSERT INTO role_permissions (role_id, permission_id)
    SELECT id, manage_order_perm_id
    FROM roles
    WHERE name = 'SYSTEM_ADMIN'
    ON CONFLICT DO NOTHING;

    INSERT INTO role_permissions (role_id, permission_id)
    SELECT id, read_order_perm_id
    FROM roles
    WHERE name = 'SYSTEM_ADMIN'
    ON CONFLICT DO NOTHING;

END $$;
