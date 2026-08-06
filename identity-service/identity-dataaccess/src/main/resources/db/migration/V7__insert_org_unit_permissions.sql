-- V7__insert_org_unit_permissions.sql

INSERT INTO permissions (id, code, description) VALUES
(gen_random_uuid(), 'can_assign_user_to_org_unit', 'Gives user the ability to assign users to organizational units'),
(gen_random_uuid(), 'can_unassign_user_from_org_unit', 'Gives user the ability to unassign users from organizational units');

-- Assign these to SYSTEM_ADMIN
DO $$
DECLARE
    system_admin_id UUID;
    assign_perm_id UUID;
    unassign_perm_id UUID;
BEGIN
    SELECT id INTO system_admin_id FROM roles WHERE name = 'SYSTEM_ADMIN';
    SELECT id INTO assign_perm_id FROM permissions WHERE code = 'can_assign_user_to_org_unit';
    SELECT id INTO unassign_perm_id FROM permissions WHERE code = 'can_unassign_user_from_org_unit';

    IF system_admin_id IS NOT NULL THEN
        INSERT INTO role_permissions (role_id, permission_id) VALUES (system_admin_id, assign_perm_id);
        INSERT INTO role_permissions (role_id, permission_id) VALUES (system_admin_id, unassign_perm_id);
    END IF;
END $$;
