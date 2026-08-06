package com.berkay.payment.service.application.security;

import com.berkay.payment.service.domain.ports.output.repository.RolePermissionQueryPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("paymentAuthService")
public class PaymentAuthService {

    private final RolePermissionQueryPort rolePermissionQueryPort;

    public PaymentAuthService(RolePermissionQueryPort rolePermissionQueryPort) {
        this.rolePermissionQueryPort = rolePermissionQueryPort;
    }

    public boolean hasPermission(Authentication authentication, String requiredPermission) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return false;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        List<String> roleIdsRaw = jwt.getClaimAsStringList("role_ids");

        if (roleIdsRaw == null || roleIdsRaw.isEmpty()) {
            return false;
        }

        for (String roleIdStr : roleIdsRaw) {
            try {
                UUID roleId = UUID.fromString(roleIdStr);
                Set<String> permissions = rolePermissionQueryPort.getPermissionCodesByRoleId(roleId);
                if (permissions.contains(requiredPermission)) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
            }
        }

        return false;
    }
}
