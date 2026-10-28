package com.berkay.order.service.application.security;

import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.ports.output.repository.OrderAuthProjection;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.ports.output.repository.RolePermissionQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service("orderAuthService")
public class OrderAuthService {

    private static final Logger log = LoggerFactory.getLogger(OrderAuthService.class);

    private final RolePermissionQueryPort rolePermissionQueryPort;
    private final OrderRepository orderRepository;

    public OrderAuthService(RolePermissionQueryPort rolePermissionQueryPort,
                            OrderRepository orderRepository) {
        this.rolePermissionQueryPort = rolePermissionQueryPort;
        this.orderRepository = orderRepository;
    }

    public boolean hasPermission(Authentication authentication, String requiredPermission) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            return false;
        }

        // M2M Bypass
        if ("M2M".equals(jwtAuth.getUserType())) {
            return "order_service_can_sync_roles".equals(requiredPermission);
        }

        List<UUID> roleIdsRaw = jwtAuth.getRoleIds();
        if (roleIdsRaw == null || roleIdsRaw.isEmpty()) {
            return false;
        }

        // Token'daki role_ids ile Union Set yetki birleştirmesi
        for (UUID roleId : roleIdsRaw) {
            Set<String> permissions = rolePermissionQueryPort.getPermissionCodesByRoleId(roleId);
            if (permissions.contains(requiredPermission)) {
                return true;
            }
        }

        return false;
    }

    public boolean isOwner(Authentication authentication, UUID customerId) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            return false;
        }

        // M2M Bypass
        if ("M2M".equals(jwtAuth.getUserType())) {
            return false;
        }

        UUID tokenUserId = jwtAuth.getInternalId();
        return tokenUserId != null && tokenUserId.equals(customerId);
    }

    public boolean isOrderCustomer(Authentication authentication, UUID orderId) {
        if (orderId == null) {
            log.warn("isOrderCustomer: orderId is null");
            return false;
        }
        Optional<OrderAuthProjection> projectionOpt = orderRepository.findAuthProjectionById(orderId);
        if (projectionOpt.isEmpty()) {
            log.warn("isOrderCustomer: projectionOpt is EMPTY for orderId: {}", orderId);
            return false;
        }
        OrderAuthProjection p = projectionOpt.get();
        return isOwner(authentication, p.getCustomerId());
    }

    public boolean hasPermissionForRestaurant(Authentication authentication, String requiredPermission, UUID targetRestaurantId) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            log.warn("hasPermissionForRestaurant: not JwtAuthenticationToken");
            return false;
        }

        // M2M Bypass
        if ("M2M".equals(jwtAuth.getUserType())) {
            return false;
        }

        List<UUID> roleIdsRaw = jwtAuth.getRoleIds();
        log.info("hasPermissionForRestaurant: perm={}, targetRest={}, userType={}, roleIds={}",
                requiredPermission, targetRestaurantId, jwtAuth.getUserType(), roleIdsRaw);
        if (roleIdsRaw == null || roleIdsRaw.isEmpty()) {
            return false;
        }

        for (UUID roleId : roleIdsRaw) {
            try {
                UUID roleOrgUnitId = rolePermissionQueryPort.getOrganizationalUnitIdByRoleId(roleId);
                Set<String> permissions = rolePermissionQueryPort.getPermissionCodesByRoleId(roleId);
                log.info("Evaluating roleId={}: roleOrgUnitId={}, permsCount={}, hasPerm={}",
                        roleId, roleOrgUnitId, permissions.size(), permissions.contains(requiredPermission));

                // can_reply_restaurant_review is strictly restaurant-scoped; platform-level admins cannot reply to reviews
                if ("can_reply_restaurant_review".equals(requiredPermission)) {
                    if (roleOrgUnitId != null && roleOrgUnitId.equals(targetRestaurantId) && permissions.contains(requiredPermission)) {
                        return true;
                    }
                    continue;
                }

                // SYSTEM_ADMIN Platform Duzeyi Izin Kontrolu (Bulgu 9):
                // Eger rolde orgUnitId null ise (Platform duzeyi) ve izin mevcutsa erisime izin verilir
                if (roleOrgUnitId == null && (permissions.contains(requiredPermission) || ("can_read_orders".equals(requiredPermission) && permissions.contains("can_read_all_orders")))) {
                    return true;
                }

                if (roleOrgUnitId != null && roleOrgUnitId.equals(targetRestaurantId)) {
                    if (permissions.contains(requiredPermission)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                log.warn("Exception evaluating roleId={}: {}", roleId, e.getMessage());
            }
        }
        log.warn("hasPermissionForRestaurant: DENIED perm={} on targetRest={}", requiredPermission, targetRestaurantId);
        return false;
    }

    public boolean hasPermissionForOrder(Authentication authentication, String requiredPermission, UUID orderId) {
        if (orderId == null) {
            log.warn("hasPermissionForOrder: orderId is null");
            return false;
        }
        Optional<OrderAuthProjection> projectionOpt = orderRepository.findAuthProjectionById(orderId);
        if (projectionOpt.isEmpty()) {
            log.warn("hasPermissionForOrder: projectionOpt is EMPTY for orderId: {}", orderId);
            return false;
        }
        OrderAuthProjection p = projectionOpt.get();
        log.info("hasPermissionForOrder: orderId={}, restId={}, status={}, perm={}",
                orderId, p.getRestaurantId(), p.getOrderStatus(), requiredPermission);
        return hasPermissionForRestaurant(authentication, requiredPermission, p.getRestaurantId());
    }

    public boolean hasCancelPermissionForOrder(Authentication authentication, UUID orderId) {
        if (orderId == null) {
            log.warn("hasCancelPermissionForOrder: orderId is null");
            return false;
        }
        Optional<OrderAuthProjection> projectionOpt = orderRepository.findAuthProjectionById(orderId);
        if (projectionOpt.isEmpty()) {
            log.warn("hasCancelPermissionForOrder: projectionOpt is EMPTY for orderId: {}", orderId);
            return false;
        }
        OrderAuthProjection projection = projectionOpt.get();
        OrderStatus currentStatus = projection.getOrderStatus();
        if (currentStatus == null) {
            log.warn("hasCancelPermissionForOrder: currentStatus is null for orderId: {}", orderId);
            return false;
        }

        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.CANCELLING) {
            // Cift Tiklama (Double-Click) Idempotency Destegi:
            // Eger siparis zaten iptal edilmis veya iptal surecindeyse, restorana ait herhangi bir iptal yetkisi yeterlidir.
            return hasPermissionForRestaurant(authentication, "can_cancel_waiting_order", projection.getRestaurantId())
                    || hasPermissionForRestaurant(authentication, "can_cancel_approved_order", projection.getRestaurantId())
                    || hasPermissionForRestaurant(authentication, "can_cancel_preparing_order", projection.getRestaurantId())
                    || hasPermissionForRestaurant(authentication, "can_cancel_ready_order", projection.getRestaurantId())
                    || hasPermissionForRestaurant(authentication, "can_cancel_in_delivery_order", projection.getRestaurantId());
        }

        String requiredPermission = switch (currentStatus) {
            case WAITING_FOR_APPROVAL -> "can_cancel_waiting_order";
            case APPROVED -> "can_cancel_approved_order";
            case PREPARING -> "can_cancel_preparing_order";
            case READY_FOR_PICKUP -> "can_cancel_ready_order";
            case IN_DELIVERY -> "can_cancel_in_delivery_order";
            default -> null;
        };

        log.info("hasCancelPermissionForOrder: orderId={}, status={}, requiredPerm={}, restId={}",
                orderId, currentStatus, requiredPermission, projection.getRestaurantId());

        if (requiredPermission == null) {
            return false;
        }

        return hasPermissionForRestaurant(authentication, requiredPermission, projection.getRestaurantId());
    }

    public boolean isCustomer(Authentication authentication) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            return false;
        }
        return "CUSTOMER".equals(jwtAuth.getUserType());
    }
}
