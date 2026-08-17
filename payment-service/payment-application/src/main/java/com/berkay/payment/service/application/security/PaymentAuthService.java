package com.berkay.payment.service.application.security;

import com.berkay.payment.service.domain.ports.output.repository.RolePermissionQueryPort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import com.berkay.application.security.JwtAuthenticationToken;

@Slf4j
@Service("paymentAuthService")
public class PaymentAuthService {

    private final RolePermissionQueryPort rolePermissionQueryPort;

    public PaymentAuthService(RolePermissionQueryPort rolePermissionQueryPort) {
        this.rolePermissionQueryPort = rolePermissionQueryPort;
    }

    public boolean hasPermission(Authentication authentication, String requiredPermission, UUID ownerId) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            log.warn("Authentication is null or not JwtAuthenticationToken");
            return false;
        }

        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
        
        // 1. Self-Access (Customer accessing their own wallet)
        UUID internalId = jwtToken.getInternalId();
        if (internalId != null && internalId.equals(ownerId)) {
            log.info("Self-Access granted for internal_id: {}", internalId);
            return true;
        }

        // 2. Tenant-Access (Merchant accessing their restaurant wallet)
        if ("MERCHANT".equals(jwtToken.getUserType())) {
            List<UUID> roleIds = jwtToken.getRoleIds();
            if (roleIds != null) {
                for (UUID roleId : roleIds) {
                    UUID roleOrgUnitId = rolePermissionQueryPort.getOrganizationalUnitIdByRoleId(roleId).orElse(null);
                    if (roleOrgUnitId != null && roleOrgUnitId.equals(ownerId)) {
                        return hasPermissionInRoles(jwtToken, requiredPermission, ownerId);
                    }
                }
            }
        }
        
        // 3. Admin-Access (Internal Users accessing any wallet)
        String userType = jwtToken.getUserType();
        if ("INTERNAL".equals(userType)) {
            return hasPermissionInRoles(jwtToken, requiredPermission, null);
        }

        log.warn("Access Denied: internalId={}, userType={}, requestedOwnerId={}", internalId, userType, ownerId);
        return false;
    }

    private boolean hasPermissionInRoles(JwtAuthenticationToken jwtToken, String requiredPermission, UUID targetOrgUnitId) {
        List<UUID> roleIds = jwtToken.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            log.warn("No role_ids in JWT");
            return false;
        }

        for (UUID roleId : roleIds) {
                Set<String> permissions = rolePermissionQueryPort.getPermissionCodesByRoleId(roleId);
                
                log.info("Role {} has permissions: {}", roleId, permissions);
                
                if (permissions.contains(requiredPermission)) {
                    if (targetOrgUnitId == null) {
                        log.info("Access granted via role {} for self/admin access", roleId);
                        return true; // Self or Admin access
                    }
                    
                    // For Merchant, the role must either be static (orgUnitId = null in DB)
                    // or its specific org unit must match the target.
                    UUID roleOrgUnitId = rolePermissionQueryPort.getOrganizationalUnitIdByRoleId(roleId).orElse(null);
                    if (roleOrgUnitId == null || roleOrgUnitId.equals(targetOrgUnitId)) {
                        log.info("Access granted via role {} for orgUnitId {}", roleId, targetOrgUnitId);
                        return true;
                    }
                }
        }
        log.warn("No role has required permission: {}", requiredPermission);
        return false;
    }
}
