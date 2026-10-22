-- V30__update_can_assign_role_not_restricted.sql
-- Allow 'can_assign_role' permission to be assigned to dynamic custom roles (e.g. Human Resources)
UPDATE permissions
SET is_restricted = false
WHERE code = 'can_assign_role';
