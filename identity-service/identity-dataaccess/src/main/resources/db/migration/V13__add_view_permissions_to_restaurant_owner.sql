-- V13__add_view_permissions_to_restaurant_owner.sql

DO $$
DECLARE
    view_users_perm_id UUID;
    view_roles_perm_id UUID;
    view_permissions_perm_id UUID;
BEGIN
    SELECT id INTO view_users_perm_id FROM permissions WHERE code = 'can_view_users';
    SELECT id INTO view_roles_perm_id FROM permissions WHERE code = 'can_view_roles';
    SELECT id INTO view_permissions_perm_id FROM permissions WHERE code = 'can_view_permissions';

    IF view_users_perm_id IS NOT NULL THEN
        INSERT INTO role_permissions (role_id, permission_id)
        SELECT id, view_users_perm_id
        FROM roles
        WHERE name = 'RESTAURANT_OWNER'
        ON CONFLICT DO NOTHING;
    END IF;

    IF view_roles_perm_id IS NOT NULL THEN
        INSERT INTO role_permissions (role_id, permission_id)
        SELECT id, view_roles_perm_id
        FROM roles
        WHERE name = 'RESTAURANT_OWNER'
        ON CONFLICT DO NOTHING;
    END IF;

    IF view_permissions_perm_id IS NOT NULL THEN
        INSERT INTO role_permissions (role_id, permission_id)
        SELECT id, view_permissions_perm_id
        FROM roles
        WHERE name = 'RESTAURANT_OWNER'
        ON CONFLICT DO NOTHING;
    END IF;

END $$;
