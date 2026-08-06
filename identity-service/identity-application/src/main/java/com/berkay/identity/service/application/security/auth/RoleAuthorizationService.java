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
        if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
            return false;
        }

        UserType userType = jwtToken.getUserType();

        // 1. Internal ve Customer Düz Kuralı
        if (UserType.INTERNAL.equals(userType) && targetOrganizationalUnitId != null) {
            return false; // Internal sadece global(null) işlem yapabilir.
        }
        if (UserType.CUSTOMER.equals(userType)) {
            return false; // Customer rol yönetemez.
        }

        // 2. Multi-Tenant Yetki Taraması
        for (UUID roleId : jwtToken.getRoleIds()) {
            Role role = getRoleWithCache(roleId);

            if (role != null) {
                // EĞER KULLANICI MERCHANT İSE: Bu rolün context'i (Restaurant ID) ile isteğin targetOrganizationalUnitId'si eşleşmeli!
                boolean isOrganizationalUnitMatch = true;
                if (UserType.MERCHANT.equals(userType)) {
                    if (targetOrganizationalUnitId != null) {
                        if (role.isStatic()) {
                            // Statik roller (Örn: RESTAURANT_OWNER) globaldir. Yetkinin restoranda geçerli olması için JWT'deki orgUnit listesinde bulunması gerekir.
                            isOrganizationalUnitMatch = jwtToken.getOrganizationalUnitIds() != null && jwtToken.getOrganizationalUnitIds().contains(targetOrganizationalUnitId);
                        } else {
                            // Dinamik roller doğrudan belirli bir restorana aittir.
                            isOrganizationalUnitMatch = role.getOrganizationalUnitId() != null && role.getOrganizationalUnitId().equals(targetOrganizationalUnitId);
                        }
                    } else {
                        // Merchant'lar global (orgUnitId = null) işlem YAPAMAZLAR!
                        isOrganizationalUnitMatch = false;
                    }
                }

                    if (isOrganizationalUnitMatch) {
                        // Rolün içindeki yetkilerde (örn: can_create_role) var mı kontrol et
                        boolean hasPerm = role.getPermissions() != null && role.getPermissions().stream()
                                .anyMatch(p -> p.getCode().equalsIgnoreCase(action) && p.isActive());

                        if (hasPerm) {
                            return true; // İlk uygun rolde kapı açılır.
                        }
                    }
            }
        }

        return false; // Hiçbir rolünde bu yetki yok veya context'leri eşleşmedi.
    }
}