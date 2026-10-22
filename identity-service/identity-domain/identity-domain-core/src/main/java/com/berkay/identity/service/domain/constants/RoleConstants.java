package com.berkay.identity.service.domain.constants;

import java.util.List;
import java.util.Map;

public class RoleConstants {
    private RoleConstants() {}

    public static final String CUSTOMER_BASE = "CUSTOMER_BASE";
    public static final String MERCHANT_BASE = "MERCHANT_BASE";
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String RESTAURANT_OWNER = "RESTAURANT_OWNER";

    public static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
            SYSTEM_ADMIN, List.of(
                    PermissionConstants.CAN_ASSIGN_ROLE,
                    PermissionConstants.CAN_RESET_PASSWORD,
                    PermissionConstants.CAN_CREATE_ROLE,
                    PermissionConstants.CAN_UPDATE_ROLE,
                    PermissionConstants.CAN_DELETE_ROLE,
                    PermissionConstants.CAN_MANAGE_PAYMENT,
                    PermissionConstants.CAN_READ_PAYMENT,
                    PermissionConstants.CAN_VIEW_USERS,
                    PermissionConstants.CAN_VIEW_ROLES,
                    PermissionConstants.CAN_VIEW_PERMISSIONS,
                    PermissionConstants.RESTAURANT_SERVICE_CAN_SYNC_ROLES,
                    PermissionConstants.PAYMENT_SERVICE_CAN_SYNC_ROLES,
                    PermissionConstants.ORDER_SERVICE_CAN_SYNC_ROLES,
                    PermissionConstants.CAN_FORCE_CANCEL_ORDER,
                    PermissionConstants.CAN_READ_ALL_ORDERS
            ),
            CUSTOMER_BASE, List.of(
            ),
            MERCHANT_BASE, List.of(
                    PermissionConstants.CAN_MANAGE_RESTAURANT
            ),
            RESTAURANT_OWNER, List.of(
                    PermissionConstants.CAN_MANAGE_RESTAURANT,
                    PermissionConstants.CAN_MANAGE_CATEGORY,
                    PermissionConstants.CAN_CREATE_ROLE,
                    PermissionConstants.CAN_UPDATE_ROLE,
                    PermissionConstants.CAN_DELETE_ROLE,
                    PermissionConstants.CAN_ASSIGN_ROLE,
                    PermissionConstants.CAN_CREATE_PRODUCT,
                    PermissionConstants.CAN_UPDATE_PRODUCT,
                    PermissionConstants.CAN_DELETE_PRODUCT,
                    PermissionConstants.CAN_ADD_PERSONNEL,
                    PermissionConstants.CAN_REMOVE_PERSONNEL,
                    PermissionConstants.CAN_VIEW_MERCHANT_USERS,
                    PermissionConstants.CAN_VIEW_USERS,
                    PermissionConstants.CAN_VIEW_ROLES,
                    PermissionConstants.CAN_VIEW_PERMISSIONS,
                    PermissionConstants.CAN_MANAGE_PAYMENT,
                    PermissionConstants.CAN_READ_PAYMENT,
                    PermissionConstants.CAN_READ_ORDERS,
                    PermissionConstants.CAN_APPROVE_ORDER,
                    PermissionConstants.CAN_CANCEL_WAITING_ORDER,
                    PermissionConstants.CAN_START_PREPARING_ORDER,
                    PermissionConstants.CAN_CANCEL_APPROVED_ORDER,
                    PermissionConstants.CAN_READY_ORDER,
                    PermissionConstants.CAN_CANCEL_PREPARING_ORDER,
                    PermissionConstants.CAN_START_DELIVERY,
                    PermissionConstants.CAN_CANCEL_READY_ORDER,
                    PermissionConstants.CAN_DELIVER_ORDER,
                    PermissionConstants.CAN_CANCEL_IN_DELIVERY_ORDER
            )
    );
}