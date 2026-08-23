package com.berkay.restaurant.service.application.security;

import com.berkay.restaurant.service.domain.ports.output.repository.RolePermissionQueryPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("restaurantAuthService")
public class RestaurantAuthService {

    private final RolePermissionQueryPort rolePermissionQueryPort;

    public RestaurantAuthService(RolePermissionQueryPort rolePermissionQueryPort) {
        this.rolePermissionQueryPort = rolePermissionQueryPort;
    }

    public boolean hasPermission(Authentication authentication, String requiredPermission) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            return false;
        }

        // M2M Bypass
        if ("M2M".equals(jwtAuth.getUserType())) {
            return "restaurant_service_can_sync_roles".equals(requiredPermission);
        }

        List<UUID> roleIdsRaw = jwtAuth.getRoleIds();
        if (roleIdsRaw == null || roleIdsRaw.isEmpty()) {
            return false;
        }

        for (UUID roleId : roleIdsRaw) {
            try {
                Set<String> permissions = rolePermissionQueryPort.getPermissionCodesByRoleId(roleId);
                if (permissions.contains(requiredPermission)) {
                    return true;
                }
            } catch (IllegalArgumentException e) {}
        }
        return false;
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

    public boolean isMerchant(Authentication authentication) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            return false;
        }
        return "MERCHANT".equals(jwtAuth.getUserType());
    }

    public boolean isMemberOfRestaurant(Authentication authentication, UUID targetRestaurantId) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            return false;
        }

        if (!"MERCHANT".equals(jwtAuth.getUserType())) {
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
                    return true;
                }
            } catch (IllegalArgumentException e) {}
        }
        return false;
    }
}
