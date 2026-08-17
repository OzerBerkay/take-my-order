-- V15__fix_missing_restaurant_owner_payment_permissions.sql

DO $$
DECLARE
    manage_payment_perm_id UUID;
    read_payment_perm_id UUID;
BEGIN
    SELECT id INTO manage_payment_perm_id FROM permissions WHERE code = 'can_manage_payment';
    SELECT id INTO read_payment_perm_id FROM permissions WHERE code = 'can_read_payment';

    IF manage_payment_perm_id IS NULL OR read_payment_perm_id IS NULL THEN
        RETURN;
    END IF;

    -- Add 'can_manage_payment' and 'can_read_payment' to all RESTAURANT_OWNER roles
    -- This handles any dynamic roles created after V14 but before RoleConstants was updated.
    INSERT INTO role_permissions (role_id, permission_id)
    SELECT id, manage_payment_perm_id
    FROM roles
    WHERE name = 'RESTAURANT_OWNER'
    ON CONFLICT DO NOTHING;

    INSERT INTO role_permissions (role_id, permission_id)
    SELECT id, read_payment_perm_id
    FROM roles
    WHERE name = 'RESTAURANT_OWNER'
    ON CONFLICT DO NOTHING;

    -- Also ensure SYSTEM_ADMIN has them, just in case
    INSERT INTO role_permissions (role_id, permission_id)
    SELECT id, manage_payment_perm_id
    FROM roles
    WHERE name = 'SYSTEM_ADMIN'
    ON CONFLICT DO NOTHING;

    INSERT INTO role_permissions (role_id, permission_id)
    SELECT id, read_payment_perm_id
    FROM roles
    WHERE name = 'SYSTEM_ADMIN'
    ON CONFLICT DO NOTHING;

END $$;
