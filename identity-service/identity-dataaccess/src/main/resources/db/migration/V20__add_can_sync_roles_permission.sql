-- V20__add_can_sync_roles_permission.sql

INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
SELECT gen_random_uuid(), 'can_sync_roles', 'Gives ability to sync roles to other microservices', 'SYSTEM', true, true
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'can_sync_roles');

DO $$
DECLARE
    sync_roles_perm_id UUID;
BEGIN
    SELECT id INTO sync_roles_perm_id FROM permissions WHERE code = 'can_sync_roles';

    IF sync_roles_perm_id IS NULL THEN
        RETURN;
    END IF;

    -- Add permissions to SYSTEM_ADMIN role
    INSERT INTO role_permissions (role_id, permission_id)
    SELECT id, sync_roles_perm_id
    FROM roles
    WHERE name = 'SYSTEM_ADMIN'
    ON CONFLICT DO NOTHING;

END $$;
