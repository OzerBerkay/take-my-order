package com.berkay.identity.service.domain.constants;

public final class PermissionConstants {
    private PermissionConstants() {
    }

    // Identity Permissions
    public static final String CAN_ASSIGN_ROLE = "can_assign_role";
    public static final String CAN_RESET_PASSWORD = "can_reset_password";
    public static final String CAN_CREATE_ROLE = "can_create_role";
    public static final String CAN_UPDATE_ROLE = "can_update_role";
    public static final String CAN_DELETE_ROLE = "can_delete_role";
    
    // Domain Sync Permissions
    public static final String RESTAURANT_SERVICE_CAN_SYNC_ROLES = "restaurant_service_can_sync_roles";
    public static final String PAYMENT_SERVICE_CAN_SYNC_ROLES = "payment_service_can_sync_roles";
    public static final String ORDER_SERVICE_CAN_SYNC_ROLES = "order_service_can_sync_roles";

    // Order Permissions
    public static final String CAN_READ_ORDERS = "can_read_orders";

    // Restaurant Permissions
    public static final String CAN_MANAGE_RESTAURANT = "can_manage_restaurant";
    public static final String CAN_MANAGE_CATEGORY = "can_manage_category";
    public static final String CAN_CREATE_PRODUCT = "can_create_product";
    public static final String CAN_UPDATE_PRODUCT = "can_update_product";
    public static final String CAN_DELETE_PRODUCT = "can_delete_product";
    public static final String CAN_ADD_PERSONNEL = "can_add_personnel";
    public static final String CAN_REMOVE_PERSONNEL = "can_remove_personnel";
    public static final String CAN_REVIEW_ORDERS = "can_review_orders";

    // Payment Permissions
    public static final String CAN_MANAGE_PAYMENT = "can_manage_payment";
    public static final String CAN_READ_PAYMENT = "can_read_payment";

    // System & Admin Permissions
    public static final String CAN_MANAGE_SYSTEM_SETTINGS = "can_manage_system_settings";
    public static final String CAN_REVOKE_USER = "can_revoke_user";

    // View Permissions
    public static final String CAN_VIEW_USERS = "can_view_users";
    public static final String CAN_VIEW_MERCHANT_USERS = "can_view_merchant_users";
    public static final String CAN_VIEW_ROLES = "can_view_roles";
    public static final String CAN_VIEW_PERMISSIONS = "can_view_permissions";
}
