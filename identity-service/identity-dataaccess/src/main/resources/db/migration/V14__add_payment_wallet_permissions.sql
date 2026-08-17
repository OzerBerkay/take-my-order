-- V14__add_payment_wallet_permissions.sql

-- 1. Insert 'can_manage_payment' and 'can_read_payment' permissions if they do not exist
INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
SELECT gen_random_uuid(), 'can_manage_payment', 'Gives ability to manage and perform transactions on wallets', 'PAYMENT', true, true
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'can_manage_payment');

INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
SELECT gen_random_uuid(), 'can_read_payment', 'Gives ability to read wallet balances and transaction history', 'PAYMENT', true, false
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'can_read_payment');

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

    -- 2. Add 'can_manage_payment' and 'can_read_payment' to all RESTAURANT_OWNER roles
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

    -- 3. Add 'can_manage_payment' and 'can_read_payment' to SYSTEM_ADMIN role
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
