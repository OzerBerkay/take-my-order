package com.berkay.identity.service.application.security.auth;

import com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("roleAuthService")
@RequiredArgsConstructor
public class RoleAuthorizationService {

    private final RoleRepository roleRepository;

    @Cacheable(value = "roles_cache", key = "#roleId")
    public Role getRoleWithCache(UUID roleId) {
        log.debug("CACHE MISS! Fetching Role {} from Database...", roleId);
        return roleRepository.findById(new RoleId(roleId)).orElse(null);
    }

    @CacheEvict(value = "roles_cache", key = "#roleId")
    public void evictRoleCache(UUID roleId) {
        log.debug("Evicting Role {} from roles_cache...", roleId);
    }

    public boolean canCreateRole(Authentication authentication, UUID targetOrganizationalUnitId) {
        return checkPermission(authentication, targetOrganizationalUnitId, "can_create_role");
    }

    public boolean canUpdateRole(Authentication authentication, UUID roleId) {
        if (roleId == null) {
            log.warn("canUpdateRole failed: roleId is null");
            return false;
        }
        Role role = getRoleWithCache(roleId);
        if (role == null) {
            log.warn("canUpdateRole failed: role not found with id {}", roleId);
            return false;
        }
        if (role.isStatic()) {
            log.warn("canUpdateRole failed: Static roles cannot be updated! RoleId: {}", roleId);
            return false;
        }
        return checkPermission(authentication, role.getOrganizationalUnitId(), "can_update_role");
    }

    public boolean canUpdateRole(Authentication authentication, UUID roleId, UUID targetOrganizationalUnitId) {
        if (roleId == null) {
            return false;
        }
        Role role = getRoleWithCache(roleId);
        if (role == null || role.isStatic()) {
            return false;
        }
        if (targetOrganizationalUnitId != null && !targetOrganizationalUnitId.equals(role.getOrganizationalUnitId())) {
            log.warn("canUpdateRole failed: provided targetOrgId {} does not match role orgUnitId {}", targetOrganizationalUnitId, role.getOrganizationalUnitId());
            return false;
        }
        return checkPermission(authentication, role.getOrganizationalUnitId(), "can_update_role");
    }

    public boolean canDeleteRole(Authentication authentication, UUID roleId) {
        if (roleId == null) {
            log.warn("canDeleteRole failed: roleId is null");
            return false;
        }
        Role role = getRoleWithCache(roleId);
        if (role == null) {
            log.warn("canDeleteRole failed: role not found with id {}", roleId);
            return false;
        }
        if (role.isStatic()) {
            log.warn("canDeleteRole failed: Static roles cannot be deleted! RoleId: {}", roleId);
            return false;
        }
        return checkPermission(authentication, role.getOrganizationalUnitId(), "can_delete_role");
    }

    public boolean canDeleteRole(Authentication authentication, UUID roleId, UUID targetOrganizationalUnitId) {
        if (roleId == null) {
            return false;
        }
        Role role = getRoleWithCache(roleId);
        if (role == null || role.isStatic()) {
            return false;
        }
        if (targetOrganizationalUnitId != null && !targetOrganizationalUnitId.equals(role.getOrganizationalUnitId())) {
            log.warn("canDeleteRole failed: provided targetOrgId {} does not match role orgUnitId {}", targetOrganizationalUnitId, role.getOrganizationalUnitId());
            return false;
        }
        return checkPermission(authentication, role.getOrganizationalUnitId(), "can_delete_role");
    }

    public boolean hasPermission(Authentication authentication, String permissionCode) {
        return checkPermission(authentication, null, permissionCode);
    }

    public boolean hasPermissionForOrg(Authentication authentication, UUID targetOrganizationalUnitId, String permissionCode) {
        return checkPermission(authentication, targetOrganizationalUnitId, permissionCode);
    }

    /**
     * Kullanıcının token'ındaki roller üzerinden hedef bağlamda (context) istenen eylemi (action)
     * yapmaya yetkili olup olmadığını kontrol eder.
     *
     * @param targetOrganizationalUnitId İşlem yapılan hedefin bağlamı (Restoran ID). Global işlemler için null.
     * @param action Kontrol edilecek yetki kodu (örn: can_create_role, can_update_role, can_delete_role).
     */
    private boolean checkPermission(Authentication authentication, UUID targetOrganizationalUnitId, String action) {
        log.debug("RoleAuthorizationService: checkPermission called for action={}, targetOrg={}", action, targetOrganizationalUnitId);

        if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
            log.warn("checkPermission failed: authentication is not JwtAuthenticationToken.");
            return false;
        }

        UserType userType = jwtToken.getUserType();

        if (UserType.M2M.equals(userType)) {
            return true;
        }

        // 1. CUSTOMER asla rol yönetimi veya idari yetki kullanamaz
        if (UserType.CUSTOMER.equals(userType)) {
            log.warn("checkPermission failed: CUSTOMER cannot manage roles");
            return false;
        }

        // 2. INTERNAL kullanıcı yalnızca global rolleri yönetebilir (targetOrganizationalUnitId == null olmalıdır)
        if (UserType.INTERNAL.equals(userType)) {
            if (targetOrganizationalUnitId != null) {
                log.warn("checkPermission failed: INTERNAL user cannot have targetOrganizationalUnitId");
                return false;
            }

            for (UUID roleId : jwtToken.getRoleIds()) {
                Role role = getRoleWithCache(roleId);
                if (role != null && role.getOrganizationalUnitId() == null) {
                    boolean hasPerm = role.getPermissions() != null && role.getPermissions().stream()
                            .anyMatch(p -> p.getCode().equalsIgnoreCase(action) && p.isActive());
                    if (hasPerm) {
                        return true;
                    }
                }
            }
            log.warn("checkPermission failed: no suitable global role found for action={}", action);
            return false;
        }

        // 3. MERCHANT kullanıcısı mutlak suretle bir hedef restoran bağlamında işlem yapmalıdır
        if (UserType.MERCHANT.equals(userType)) {
            if (targetOrganizationalUnitId == null) {
                log.warn("checkPermission failed: MERCHANT must specify a targetOrganizationalUnitId");
                return false;
            }

            for (UUID roleId : jwtToken.getRoleIds()) {
                Role role = getRoleWithCache(roleId);
                if (role != null && role.getOrganizationalUnitId() != null 
                        && role.getOrganizationalUnitId().equals(targetOrganizationalUnitId)) {
                    boolean hasPerm = role.getPermissions() != null && role.getPermissions().stream()
                            .anyMatch(p -> p.getCode().equalsIgnoreCase(action) && p.isActive());
                    if (hasPerm) {
                        return true;
                    }
                }
            }
            log.warn("checkPermission failed: no role belonging to restaurant {} has permission {}", targetOrganizationalUnitId, action);
            return false;
        }

        return false;
    }
}