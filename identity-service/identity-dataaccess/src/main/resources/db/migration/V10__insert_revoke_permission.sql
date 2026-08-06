-- V5__insert_revoke_permission.sql

INSERT INTO permissions (id, code, description, domain, active, is_restricted) VALUES
('019fb322-1617-74e0-b073-900000000020', 'can_revoke_user', 'can_revoke_user', 'IDENTITY', true, false);

-- Map to SYSTEM_ADMIN role
INSERT INTO role_permissions (role_id, permission_id) VALUES
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000020');
