-- V21__refactor_and_clean_permissions.sql

DO $$
DECLARE
    can_sync_roles_id UUID;
    can_manage_order_id UUID;
    can_read_order_id UUID;
    
    restaurant_sync_id UUID;
    payment_sync_id UUID;
    order_sync_id UUID;
    
    can_review_orders_id UUID;
    
    sys_admin_role_id UUID;
    rest_owner_role_id UUID;
    customer_base_role_id UUID;
BEGIN

    -- 1. Get role IDs
    SELECT id INTO sys_admin_role_id FROM roles WHERE name = 'SYSTEM_ADMIN';
    SELECT id INTO rest_owner_role_id FROM roles WHERE name = 'RESTAURANT_OWNER';
    SELECT id INTO customer_base_role_id FROM roles WHERE name = 'CUSTOMER_BASE';

    -- 2. DELETE 'can_sync_roles' (from V20)
    SELECT id INTO can_sync_roles_id FROM permissions WHERE code = 'can_sync_roles';
    IF can_sync_roles_id IS NOT NULL THEN
        DELETE FROM role_permissions WHERE permission_id = can_sync_roles_id;
        DELETE FROM permissions WHERE id = can_sync_roles_id;
    END IF;

    -- 3. DELETE 'can_manage_order' (from V19)
    SELECT id INTO can_manage_order_id FROM permissions WHERE code = 'can_manage_order';
    IF can_manage_order_id IS NOT NULL THEN
        DELETE FROM role_permissions WHERE permission_id = can_manage_order_id;
        DELETE FROM permissions WHERE id = can_manage_order_id;
    END IF;

    -- 4. UPDATE 'can_read_order' to 'can_read_orders' and set domain 'ORDER' (from V17 & V19)
    SELECT id INTO can_read_order_id FROM permissions WHERE code = 'can_read_order';
    IF can_read_order_id IS NOT NULL THEN
        UPDATE permissions SET code = 'can_read_orders', domain = 'ORDER' WHERE id = can_read_order_id;
        
        -- Remove from CUSTOMER_BASE
        IF customer_base_role_id IS NOT NULL THEN
            DELETE FROM role_permissions WHERE permission_id = can_read_order_id AND role_id = customer_base_role_id;
        END IF;
    END IF;
    
    -- In case 'can_read_orders' already exists (or changed manually), try to get it
    IF can_read_order_id IS NULL THEN
        SELECT id INTO can_read_order_id FROM permissions WHERE code = 'can_read_orders';
        IF can_read_order_id IS NOT NULL THEN
            UPDATE permissions SET domain = 'ORDER' WHERE id = can_read_order_id;
            IF customer_base_role_id IS NOT NULL THEN
                DELETE FROM role_permissions WHERE permission_id = can_read_order_id AND role_id = customer_base_role_id;
            END IF;
        END IF;
    END IF;

    -- 5. INSERT 3 new sync permissions
    restaurant_sync_id := gen_random_uuid();
    payment_sync_id := gen_random_uuid();
    order_sync_id := gen_random_uuid();

    INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
    SELECT restaurant_sync_id, 'restaurant_service_can_sync_roles', 'Restaurant domain role sync', 'RESTAURANT', true, true
    WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'restaurant_service_can_sync_roles');

    INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
    SELECT payment_sync_id, 'payment_service_can_sync_roles', 'Payment domain role sync', 'PAYMENT', true, true
    WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'payment_service_can_sync_roles');

    INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
    SELECT order_sync_id, 'order_service_can_sync_roles', 'Order domain role sync', 'ORDER', true, true
    WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'order_service_can_sync_roles');

    -- Assign sync perms to SYSTEM_ADMIN
    IF sys_admin_role_id IS NOT NULL THEN
        -- Get real IDs in case they already existed
        SELECT id INTO restaurant_sync_id FROM permissions WHERE code = 'restaurant_service_can_sync_roles';
        SELECT id INTO payment_sync_id FROM permissions WHERE code = 'payment_service_can_sync_roles';
        SELECT id INTO order_sync_id FROM permissions WHERE code = 'order_service_can_sync_roles';

        INSERT INTO role_permissions (role_id, permission_id) VALUES (sys_admin_role_id, restaurant_sync_id) ON CONFLICT DO NOTHING;
        INSERT INTO role_permissions (role_id, permission_id) VALUES (sys_admin_role_id, payment_sync_id) ON CONFLICT DO NOTHING;
        INSERT INTO role_permissions (role_id, permission_id) VALUES (sys_admin_role_id, order_sync_id) ON CONFLICT DO NOTHING;
    END IF;

    -- 6. INSERT 'can_review_orders'
    can_review_orders_id := gen_random_uuid();
    INSERT INTO permissions (id, code, description, domain, active, is_restricted) 
    SELECT can_review_orders_id, 'can_review_orders', 'Gives ability to approve or reject restaurant orders', 'RESTAURANT', true, false
    WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'can_review_orders');
    
    SELECT id INTO can_review_orders_id FROM permissions WHERE code = 'can_review_orders';

    -- Assign to RESTAURANT_OWNER and SYSTEM_ADMIN
    IF can_review_orders_id IS NOT NULL THEN
        IF rest_owner_role_id IS NOT NULL THEN
            INSERT INTO role_permissions (role_id, permission_id) VALUES (rest_owner_role_id, can_review_orders_id) ON CONFLICT DO NOTHING;
        END IF;
        IF sys_admin_role_id IS NOT NULL THEN
            INSERT INTO role_permissions (role_id, permission_id) VALUES (sys_admin_role_id, can_review_orders_id) ON CONFLICT DO NOTHING;
        END IF;
    END IF;

END $$;
