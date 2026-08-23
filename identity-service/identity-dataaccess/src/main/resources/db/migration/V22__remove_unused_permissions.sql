-- Remove unused permissions from role_permissions
DELETE FROM role_permissions WHERE permission_id IN (
    SELECT id FROM permissions WHERE code IN (
        'can_update_user',
        'can_suspend_merchant',
        'can_manage_customers',
        'can_audit_system_logs',
        'can_assign_user_to_org_unit',
        'can_unassign_user_from_org_unit',
        'can_view_order_reports',
        'can_create_order',
        'can_view_restaurant_orders',
        'can_update_order_status',
        'can_view_all_orders',
        'can_view_payment_reports',
        'can_refund_payment',
        'can_delete_menu',
        'can_manage_merchants',
        'can_approve_restaurant',
        'can_reject_restaurant',
        'can_create_menu',
        'can_update_menu'
    )
);

-- Remove unused permissions from permissions table
DELETE FROM permissions WHERE code IN (
        'can_update_user',
        'can_suspend_merchant',
        'can_manage_customers',
        'can_audit_system_logs',
        'can_assign_user_to_org_unit',
        'can_unassign_user_from_org_unit',
        'can_view_order_reports',
        'can_create_order',
        'can_view_restaurant_orders',
        'can_update_order_status',
        'can_view_all_orders',
        'can_view_payment_reports',
        'can_refund_payment',
        'can_delete_menu',
        'can_manage_merchants',
        'can_approve_restaurant',
        'can_reject_restaurant',
        'can_create_menu',
        'can_update_menu'
);
