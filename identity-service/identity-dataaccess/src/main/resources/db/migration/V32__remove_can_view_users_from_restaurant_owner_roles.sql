-- V32__remove_can_view_users_from_restaurant_owner_roles.sql

DO $$
DECLARE
    view_users_perm_id UUID;
BEGIN
    SELECT id INTO view_users_perm_id FROM permissions WHERE code = 'can_view_users';

    IF view_users_perm_id IS NOT NULL THEN
        -- Remove can_view_users from all RESTAURANT_OWNER roles and any merchant-owned roles
        DELETE FROM role_permissions
        WHERE permission_id = view_users_perm_id
          AND role_id IN (
              SELECT id FROM roles 
              WHERE user_type = 'MERCHANT' OR name = 'RESTAURANT_OWNER'
          );
    END IF;
END $$;

