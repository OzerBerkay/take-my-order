-- V4__insert_initial_roles.sql

-- 1. Fix Permissions Table Schema (V1 created 'name' instead of 'code', and missing fields)
ALTER TABLE permissions RENAME COLUMN name TO code;
ALTER TABLE permissions ADD COLUMN description VARCHAR(500);
ALTER TABLE permissions ADD COLUMN domain VARCHAR(50) NOT NULL DEFAULT 'IDENTITY';
ALTER TABLE permissions ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE permissions ADD COLUMN is_restricted BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Insert Permissions
INSERT INTO permissions (id, code, description, domain, active, is_restricted) VALUES
('b3010b9a-47af-4b82-8418-4720e3ad5d31', 'can_update_user', 'can_update_user', 'IDENTITY', true, false),
('16cc7595-5dbd-42bc-9d32-26cbba95fddb', 'can_update_user_status', 'can_update_user_status', 'IDENTITY', true, true),
('f199341c-3b99-4d69-a790-2e8ec5d1607a', 'can_assign_role', 'can_assign_role', 'IDENTITY', true, true),
('80b5bca1-f050-4d43-ae62-e6e2321568c0', 'can_reset_password', 'can_reset_password', 'IDENTITY', true, true),
('abf9a3e2-c0e6-4bf4-ad58-0cb93481e1a5', 'can_create_role', 'can_create_role', 'IDENTITY', true, true),
('d40ad2f1-6a2c-47fc-8f6a-493ce745c11d', 'can_update_role', 'can_update_role', 'IDENTITY', true, true),
('1f3e79bd-197e-4050-98db-4e788bc55941', 'can_delete_role', 'can_delete_role', 'IDENTITY', true, true),
('707d7211-131c-43da-912a-3fb7cf8e69d9', 'can_create_order', 'can_create_order', 'ORDER', true, false),
('9825b4ec-6a32-4d45-9ec4-c4bfa6c28fcd', 'can_manage_restaurant', 'can_manage_restaurant', 'RESTAURANT', true, false),
('019fb322-1617-74e0-b073-900000000001', 'can_suspend_merchant', 'can_suspend_merchant', 'IDENTITY', true, true),
('019fb322-1617-74e0-b073-900000000002', 'can_approve_restaurant', 'can_approve_restaurant', 'RESTAURANT', true, true),
('019fb322-1617-74e0-b073-900000000003', 'can_reject_restaurant', 'can_reject_restaurant', 'RESTAURANT', true, true),
('019fb322-1617-74e0-b073-900000000004', 'can_view_all_orders', 'can_view_all_orders', 'ORDER', true, true),
('019fb322-1617-74e0-b073-900000000005', 'can_refund_payment', 'can_refund_payment', 'PAYMENT', true, true),
('019fb322-1617-74e0-b073-900000000006', 'can_create_menu', 'can_create_menu', 'RESTAURANT', true, false),
('019fb322-1617-74e0-b073-900000000007', 'can_update_menu', 'can_update_menu', 'RESTAURANT', true, false),
('019fb322-1617-74e0-b073-900000000008', 'can_delete_menu', 'can_delete_menu', 'RESTAURANT', true, false),
('019fb322-1617-74e0-b073-900000000009', 'can_create_product', 'can_create_product', 'RESTAURANT', true, false),
('019fb322-1617-74e0-b073-900000000010', 'can_update_product', 'can_update_product', 'RESTAURANT', true, false),
('019fb322-1617-74e0-b073-900000000011', 'can_delete_product', 'can_delete_product', 'RESTAURANT', true, false),
('019fb322-1617-74e0-b073-900000000012', 'can_view_restaurant_orders', 'can_view_restaurant_orders', 'ORDER', true, false),
('019fb322-1617-74e0-b073-900000000013', 'can_update_order_status', 'can_update_order_status', 'ORDER', true, false),
('019fb322-1617-74e0-b073-900000000014', 'can_manage_system_settings', 'can_manage_system_settings', 'IDENTITY', true, false),
('019fb322-1617-74e0-b073-900000000015', 'can_view_order_reports', 'can_view_order_reports', 'ORDER', true, false),
('019fb322-1617-74e0-b073-900000000019', 'can_view_payment_reports', 'can_view_payment_reports', 'PAYMENT', true, false),
('019fb322-1617-74e0-b073-900000000016', 'can_manage_merchants', 'can_manage_merchants', 'RESTAURANT', true, false),
('019fb322-1617-74e0-b073-900000000017', 'can_manage_customers', 'can_manage_customers', 'IDENTITY', true, false),
('019fb322-1617-74e0-b073-900000000018', 'can_audit_system_logs', 'can_audit_system_logs', 'IDENTITY', true, false);

-- 3. Insert Static Roles
INSERT INTO roles (id, name, user_type, is_static, created_by_user_id, version, created_at, updated_at) VALUES
('019fb322-1617-74e0-b073-93b0948ea0dc', 'SYSTEM_ADMIN', 'INTERNAL', true, '00000000-0000-0000-0000-000000000000', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('019fb322-1617-74e0-b073-95d9b9bb0fdf', 'CUSTOMER_BASE', 'CUSTOMER', true, '00000000-0000-0000-0000-000000000000', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('019fb322-1617-74e0-b073-9aec2675dbaa', 'MERCHANT_BASE', 'MERCHANT', true, '00000000-0000-0000-0000-000000000000', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. Map Permissions to Roles
-- SYSTEM_ADMIN -> can_update_user, can_update_user_status, can_assign_role, can_reset_password, can_create_role, can_update_role, can_delete_role
INSERT INTO role_permissions (role_id, permission_id) VALUES
('019fb322-1617-74e0-b073-93b0948ea0dc', 'b3010b9a-47af-4b82-8418-4720e3ad5d31'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '16cc7595-5dbd-42bc-9d32-26cbba95fddb'),
('019fb322-1617-74e0-b073-93b0948ea0dc', 'f199341c-3b99-4d69-a790-2e8ec5d1607a'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '80b5bca1-f050-4d43-ae62-e6e2321568c0'),
('019fb322-1617-74e0-b073-93b0948ea0dc', 'abf9a3e2-c0e6-4bf4-ad58-0cb93481e1a5'),
('019fb322-1617-74e0-b073-93b0948ea0dc', 'd40ad2f1-6a2c-47fc-8f6a-493ce745c11d'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '1f3e79bd-197e-4050-98db-4e788bc55941'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000001'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000002'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000003'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000004'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000005'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000014'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000015'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000016'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000017'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000018'),
('019fb322-1617-74e0-b073-93b0948ea0dc', '019fb322-1617-74e0-b073-900000000019');

-- CUSTOMER_BASE -> can_create_order
INSERT INTO role_permissions (role_id, permission_id) VALUES
('019fb322-1617-74e0-b073-95d9b9bb0fdf', '707d7211-131c-43da-912a-3fb7cf8e69d9');

-- MERCHANT_BASE -> can_manage_restaurant
INSERT INTO role_permissions (role_id, permission_id) VALUES
('019fb322-1617-74e0-b073-9aec2675dbaa', '9825b4ec-6a32-4d45-9ec4-c4bfa6c28fcd');
