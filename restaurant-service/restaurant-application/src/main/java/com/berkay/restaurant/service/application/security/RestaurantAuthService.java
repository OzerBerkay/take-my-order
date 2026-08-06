package com.berkay.restaurant.service.application.security;

import com.berkay.restaurant.service.domain.ports.output.repository.RolePermissionQueryPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("restaurantAuthService")
public class RestaurantAuthService {

    private final RolePermissionQueryPort rolePermissionQueryPort;

    public RestaurantAuthService(RolePermissionQueryPort rolePermissionQueryPort) {
        this.rolePermissionQueryPort = rolePermissionQueryPort;
    }

    public boolean hasPermission(Authentication authentication, String requiredPermission) {
        if (authentication == null || !(authentication instanceof com.berkay.application.security.JwtAuthenticationToken)) {
            return false;
        }

        com.berkay.application.security.JwtAuthenticationToken jwtAuth = (com.berkay.application.security.JwtAuthenticationToken) authentication;
        List<UUID> roleIds = jwtAuth.getRoleIds();

        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }

        for (UUID roleId : roleIds) {
            Set<String> permissions = rolePermissionQueryPort.getPermissionCodesByRoleId(roleId);
            if (permissions.contains(requiredPermission)) {
                return true;
            }
        }

        return false;
    }
    
    public boolean hasPermissionForRestaurant(Authentication authentication, String requiredPermission, UUID targetRestaurantId) {
        if (authentication == null || !(authentication instanceof com.berkay.application.security.JwtAuthenticationToken)) {
            return false;
        }

        com.berkay.application.security.JwtAuthenticationToken jwtAuth = (com.berkay.application.security.JwtAuthenticationToken) authentication;
        List<UUID> roleIds = jwtAuth.getRoleIds();

        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }

        for (UUID roleId : roleIds) {
            UUID roleOrgUnitId = rolePermissionQueryPort.getOrganizationalUnitIdByRoleId(roleId);
            
            // Only consider the role if it belongs to the target restaurant
            if (roleOrgUnitId != null && roleOrgUnitId.equals(targetRestaurantId)) {
                Set<String> permissions = rolePermissionQueryPort.getPermissionCodesByRoleId(roleId);
                if (permissions.contains(requiredPermission)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isMerchant(Authentication authentication) {
        if (authentication == null || !(authentication instanceof com.berkay.application.security.JwtAuthenticationToken)) {
            return false;
        }

        com.berkay.application.security.JwtAuthenticationToken jwtAuth = (com.berkay.application.security.JwtAuthenticationToken) authentication;
        return "MERCHANT".equals(jwtAuth.getUserType());
    }

    public boolean isMemberOfRestaurant(Authentication authentication, UUID targetRestaurantId) {
        if (authentication == null || !(authentication instanceof com.berkay.application.security.JwtAuthenticationToken)) {
            return false;
        }

        com.berkay.application.security.JwtAuthenticationToken jwtAuth = (com.berkay.application.security.JwtAuthenticationToken) authentication;

        if (!"MERCHANT".equals(jwtAuth.getUserType())) {
            return false;
        }

        List<UUID> roleIds = jwtAuth.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }

        for (UUID roleId : roleIds) {
            UUID roleOrgUnitId = rolePermissionQueryPort.getOrganizationalUnitIdByRoleId(roleId);
            if (roleOrgUnitId != null && roleOrgUnitId.equals(targetRestaurantId)) {
                return true;
            }
        }

        return false;
    }
}
