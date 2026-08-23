package com.berkay.order.service.application.security;

import com.berkay.order.service.domain.ports.output.repository.RolePermissionQueryPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("orderAuthService")
public class OrderAuthService {

    private final RolePermissionQueryPort rolePermissionQueryPort;

    public OrderAuthService(RolePermissionQueryPort rolePermissionQueryPort) {
        this.rolePermissionQueryPort = rolePermissionQueryPort;
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

    public boolean hasPermissionForRestaurant(Authentication authentication, String requiredPermission, UUID targetRestaurantId) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            return false;
        }

        // M2M Bypass
        if ("M2M".equals(jwtAuth.getUserType())) {
            return false;
        }

        List<UUID> roleIdsRaw = jwtAuth.getRoleIds();
        if (roleIdsRaw == null || roleIdsRaw.isEmpty()) {
            return false;
        }

        for (UUID roleId : roleIdsRaw) {
            try {
                UUID roleOrgUnitId = rolePermissionQueryPort.getOrganizationalUnitIdByRoleId(roleId);
                
                if (roleOrgUnitId != null && roleOrgUnitId.equals(targetRestaurantId)) {
                    Set<String> permissions = rolePermissionQueryPort.getPermissionCodesByRoleId(roleId);
                    if (permissions.contains(requiredPermission)) {
                        return true;
                    }
                }
            } catch (IllegalArgumentException e) {}
        }
        return false;
    }

    public boolean isCustomer(Authentication authentication) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            return false;
        }
        return "CUSTOMER".equals(jwtAuth.getUserType());
    }
}
