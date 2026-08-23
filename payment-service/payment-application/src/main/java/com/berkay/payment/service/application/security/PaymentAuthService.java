package com.berkay.payment.service.application.security;

import com.berkay.payment.service.domain.ports.output.repository.RolePermissionQueryPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("paymentAuthService")
public class PaymentAuthService {

    private final RolePermissionQueryPort rolePermissionQueryPort;

    public PaymentAuthService(RolePermissionQueryPort rolePermissionQueryPort) {
        this.rolePermissionQueryPort = rolePermissionQueryPort;
    }

    public boolean hasPermission(Authentication authentication, String requiredPermission) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            return false;
        }

        // M2M Bypass
        if ("M2M".equals(jwtAuth.getUserType())) {
            return "payment_service_can_sync_roles".equals(requiredPermission);
        }

        return checkPermissionInRoles(jwtAuth.getRoleIds(), requiredPermission, null);
    }

    public boolean hasPermissionForWallet(Authentication authentication, String requiredPermission, UUID ownerId) {
        if (!(authentication instanceof com.berkay.application.security.JwtAuthenticationToken jwtAuth)) {
            return false;
        }

        // M2M Bypass
        if ("M2M".equals(jwtAuth.getUserType())) {
            return false;
        }

        // 1. Self-Access
        UUID internalId = jwtAuth.getInternalId();
        if (internalId != null && internalId.equals(ownerId)) {
            return true;
        }

        // 2. Role-Based Access
        return checkPermissionInRoles(jwtAuth.getRoleIds(), requiredPermission, ownerId);
    }

    private boolean checkPermissionInRoles(List<UUID> roleIdsRaw, String requiredPermission, UUID targetOrgUnitId) {
        if (roleIdsRaw == null || roleIdsRaw.isEmpty()) {
            return false;
        }

        for (UUID roleId : roleIdsRaw) {
            try {
                Set<String> permissions = rolePermissionQueryPort.getPermissionCodesByRoleId(roleId);
                
                if (permissions.contains(requiredPermission)) {
                    if (targetOrgUnitId == null) {
                        return true;
                    }
                    UUID roleOrgUnitId = rolePermissionQueryPort.getOrganizationalUnitIdByRoleId(roleId).orElse(null);
                    if (roleOrgUnitId == null || roleOrgUnitId.equals(targetOrgUnitId)) {
                        return true;
                    }
                }
            } catch (IllegalArgumentException e) {
            }
        }
        return false;
    }
}
