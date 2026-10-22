-- V29__remove_can_view_users_from_merchant_roles.sql

DO $$
DECLARE
    view_users_perm_id UUID;
    system_admin_role_id UUID;
BEGIN
    SELECT id INTO view_users_perm_id FROM permissions WHERE code = 'can_view_users';
    SELECT id INTO system_admin_role_id FROM roles WHERE name = 'SYSTEM_ADMIN';

    IF view_users_perm_id IS NOT NULL THEN
        -- 1. Remove can_view_users from all MERCHANT roles (both RESTAURANT_OWNER and any custom merchant roles)
        DELETE FROM role_permissions
        WHERE permission_id = view_users_perm_id
          AND role_id IN (SELECT id FROM roles WHERE user_type = 'MERCHANT');

        -- 2. Ensure can_view_users is assigned to SYSTEM_ADMIN
        IF system_admin_role_id IS NOT NULL THEN
            INSERT INTO role_permissions (role_id, permission_id)
            VALUES (system_admin_role_id, view_users_perm_id)
            ON CONFLICT DO NOTHING;
        END IF;
    END IF;

END $$;
