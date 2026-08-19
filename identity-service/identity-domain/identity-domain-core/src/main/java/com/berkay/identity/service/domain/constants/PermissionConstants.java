package com.berkay.identity.service.domain.constants;

public final class PermissionConstants {
    private PermissionConstants() {
    }

    // Identity Permissions
    public static final String CAN_UPDATE_USER = "can_update_user";
    public static final String CAN_UPDATE_USER_STATUS = "can_update_user_status";
    public static final String CAN_ASSIGN_ROLE = "can_assign_role";
    public static final String CAN_RESET_PASSWORD = "can_reset_password";
    public static final String CAN_CREATE_ROLE = "can_create_role";
    public static final String CAN_UPDATE_ROLE = "can_update_role";
    public static final String CAN_DELETE_ROLE = "can_delete_role";
    public static final String CAN_ASSIGN_USER_TO_ORG_UNIT = "can_assign_user_to_org_unit";
    public static final String CAN_UNASSIGN_USER_FROM_ORG_UNIT = "can_unassign_user_from_org_unit";
    public static final String CAN_SUSPEND_MERCHANT = "can_suspend_merchant";

    // Order Permissions
    public static final String CAN_CREATE_ORDER = "can_create_order";
    public static final String CAN_VIEW_ALL_ORDERS = "can_view_all_orders";
    public static final String CAN_VIEW_RESTAURANT_ORDERS = "can_view_restaurant_orders";
    public static final String CAN_UPDATE_ORDER_STATUS = "can_update_order_status";

    // Restaurant Permissions
    public static final String CAN_MANAGE_RESTAURANT = "can_manage_restaurant";
    public static final String CAN_APPROVE_RESTAURANT = "can_approve_restaurant";
    public static final String CAN_REJECT_RESTAURANT = "can_reject_restaurant";
    public static final String CAN_CREATE_MENU = "can_create_menu";
    public static final String CAN_UPDATE_MENU = "can_update_menu";
    public static final String CAN_DELETE_MENU = "can_delete_menu";
    public static final String CAN_CREATE_PRODUCT = "can_create_product";
    public static final String CAN_UPDATE_PRODUCT = "can_update_product";
    public static final String CAN_DELETE_PRODUCT = "can_delete_product";
    public static final String CAN_ADD_PERSONNEL = "can_add_personnel";
    public static final String CAN_REMOVE_PERSONNEL = "can_remove_personnel";

    // Payment Permissions
    public static final String CAN_REFUND_PAYMENT = "can_refund_payment";
    public static final String CAN_MANAGE_PAYMENT = "can_manage_payment";
    public static final String CAN_READ_PAYMENT = "can_read_payment";

    // System & Admin Permissions
    public static final String CAN_MANAGE_SYSTEM_SETTINGS = "can_manage_system_settings";
    public static final String CAN_VIEW_ORDER_REPORTS = "can_view_order_reports";
    public static final String CAN_VIEW_PAYMENT_REPORTS = "can_view_payment_reports";
    public static final String CAN_MANAGE_MERCHANTS = "can_manage_merchants";
    public static final String CAN_MANAGE_CUSTOMERS = "can_manage_customers";
    public static final String CAN_AUDIT_SYSTEM_LOGS = "can_audit_system_logs";
    public static final String CAN_REVOKE_USER = "can_revoke_user";

    // View Permissions
    public static final String CAN_VIEW_USERS = "can_view_users";
    public static final String CAN_VIEW_MERCHANT_USERS = "can_view_merchant_users";
    public static final String CAN_VIEW_ROLES = "can_view_roles";
    public static final String CAN_VIEW_PERMISSIONS = "can_view_permissions";
}
