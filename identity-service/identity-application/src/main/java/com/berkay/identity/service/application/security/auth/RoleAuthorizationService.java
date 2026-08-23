package com.berkay.identity.service.application.security.auth;

import com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("roleAuthService")
@RequiredArgsConstructor
public class RoleAuthorizationService {

    private final RoleRepository roleRepository;
    private final com.berkay.identity.service.ports.output.repository.UserRepository userRepository;

    @Cacheable(value = "roles_cache", key = "#roleId")
    public Role getRoleWithCache(UUID roleId) {
        log.info("CACHE MISS! Fetching Role {} from Database...", roleId);
        return roleRepository.findById(new RoleId(roleId)).orElse(null);
    }

    public boolean canCreateRole(Authentication authentication, UUID targetOrganizationalUnitId) {
        return checkPermission(authentication, targetOrganizationalUnitId, "can_create_role");
    }

    public boolean canUpdateRole(Authentication authentication, UUID targetOrganizationalUnitId) {
        return checkPermission(authentication, targetOrganizationalUnitId, "can_update_role");
    }

    public boolean canDeleteRole(Authentication authentication, UUID targetOrganizationalUnitId) {
        return checkPermission(authentication, targetOrganizationalUnitId, "can_delete_role");
    }

    public boolean hasPermission(Authentication authentication, String permissionCode) {
        System.out.println("====== hasPermission CALLED! permissionCode=" + permissionCode + " ======");
        log.info("hasPermission CALLED");
        return checkPermission(authentication, null, permissionCode);
    }

    public boolean hasPermissionForOrg(Authentication authentication, UUID targetOrganizationalUnitId, String permissionCode) {
        return checkPermission(authentication, targetOrganizationalUnitId, permissionCode);
    }

    /**
     * Kullanıcının (token'daki roleIds) belirten işlemi (action) yapmaya yetkisi var mı?
     * @param targetOrganizationalUnitId İstek yapılan hedefin Context'i (Body'den veya Path'ten gelen Restaurant ID)
     */
    private boolean checkPermission(Authentication authentication, UUID targetOrganizationalUnitId, String action) {
        log.info("RoleAuthorizationService: checkPermission called for action={}, targetOrg={}", action, targetOrganizationalUnitId);
        
        if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
            log.warn("checkPermission failed: authentication is not JwtAuthenticationToken. class: {}", authentication != null ? authentication.getClass().getName() : "null");
            return false;
        }

        UserType userType = jwtToken.getUserType();
        log.info("checkPermission: userType={}", userType);

        if (UserType.M2M.equals(userType)) {
            log.info("checkPermission: allowing M2M user");
            return true;
        }

        // 1. Internal ve Customer Düz Kuralı
        if (UserType.INTERNAL.equals(userType) && targetOrganizationalUnitId != null) {
            log.warn("checkPermission failed: INTERNAL user cannot have targetOrganizationalUnitId");
            return false; 
        }
        if (UserType.CUSTOMER.equals(userType)) {
            log.warn("checkPermission failed: CUSTOMER cannot manage roles");
            return false;
        }

        // 2. Fetch User to get authorized organizational unit IDs
        com.berkay.identity.service.domain.entity.User user = userRepository.findById(new com.berkay.identity.service.domain.valueobject.UserId(jwtToken.getInternalId())).orElse(null);
        if (user == null) {
            log.warn("checkPermission failed: user not found in DB with internalId={}", jwtToken.getInternalId());
            return false;
        }

        log.info("checkPermission: user found, iterating over roleIds: {}", jwtToken.getRoleIds());

        // 3. Multi-Tenant Yetki Taraması
        for (UUID roleId : jwtToken.getRoleIds()) {
            Role role = getRoleWithCache(roleId);
            if (role != null) {
                log.info("checkPermission: evaluating role {}", role.getName());
                boolean isOrganizationalUnitMatch = true;
                if (UserType.MERCHANT.equals(userType)) {
                    if (targetOrganizationalUnitId != null) {
                        if (role.isStatic()) {
                            isOrganizationalUnitMatch = user.getOrganizationalUnitIds() != null && user.getOrganizationalUnitIds().contains(targetOrganizationalUnitId);
                        } else {
                            isOrganizationalUnitMatch = role.getOrganizationalUnitId() != null && role.getOrganizationalUnitId().equals(targetOrganizationalUnitId);
                        }
                    } else {
                        isOrganizationalUnitMatch = false;
                    }
                }

                if (isOrganizationalUnitMatch) {
                    boolean hasPerm = role.getPermissions() != null && role.getPermissions().stream()
                            .anyMatch(p -> p.getCode().equalsIgnoreCase(action) && p.isActive());
                    log.info("checkPermission: role {} hasPerm={} for action={}", role.getName(), hasPerm, action);
                    if (hasPerm) {
                        return true;
                    }
                } else {
                    log.info("checkPermission: organizational unit mismatch for role {}", role.getName());
                }
            } else {
                log.warn("checkPermission: role {} not found in cache/DB", roleId);
            }
        }

        log.warn("checkPermission failed: no suitable role found");
        return false;
    }
}