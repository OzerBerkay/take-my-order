-- V8__insert_view_permissions.sql

INSERT INTO permissions (id, code, description) VALUES
(gen_random_uuid(), 'can_view_users', 'Gives user the ability to view user details and lists'),
(gen_random_uuid(), 'can_view_roles', 'Gives user the ability to view roles'),
(gen_random_uuid(), 'can_view_permissions', 'Gives user the ability to view permissions');

-- Assign these to SYSTEM_ADMIN
DO $$
DECLARE
    system_admin_id UUID;
    view_users_perm_id UUID;
    view_roles_perm_id UUID;
    view_permissions_perm_id UUID;
BEGIN
    SELECT id INTO system_admin_id FROM roles WHERE name = 'SYSTEM_ADMIN';
    SELECT id INTO view_users_perm_id FROM permissions WHERE code = 'can_view_users';
    SELECT id INTO view_roles_perm_id FROM permissions WHERE code = 'can_view_roles';
    SELECT id INTO view_permissions_perm_id FROM permissions WHERE code = 'can_view_permissions';

    IF system_admin_id IS NOT NULL THEN
        INSERT INTO role_permissions (role_id, permission_id) VALUES (system_admin_id, view_users_perm_id);
        INSERT INTO role_permissions (role_id, permission_id) VALUES (system_admin_id, view_roles_perm_id);
        INSERT INTO role_permissions (role_id, permission_id) VALUES (system_admin_id, view_permissions_perm_id);
    END IF;
END $$;
