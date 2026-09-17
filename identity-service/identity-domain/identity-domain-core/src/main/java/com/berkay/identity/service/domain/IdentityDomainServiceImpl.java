package com.berkay.identity.service.domain;

import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.event.*;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class IdentityDomainServiceImpl implements IdentityDomainService {
    private static final String UTC = "UTC";

    @Override
    public RoleCreatedEvent validateAndInitiateRoleCreate(Role role, List<Permission> callerPermissions) {
        // 1. Alt Küme (Subset) Kuralı Kontrolü
        validateSubsetRule(role.getPermissions(), callerPermissions);

        // 2. Role Aggregate'ine kendini başlatmasını söyle (Context ve Static kuralları içerde çalışır)
        role.initializeRole();

        // 3. Olayı oluştur ve dön
        return new RoleCreatedEvent(role, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public RoleUpdatedEvent validateAndInitiateRoleUpdate(Role role, String newName, List<Permission> newPermissions, List<Permission> callerPermissions) {
        // 1. Alt Küme (Subset) Kuralı Kontrolü
        validateSubsetRule(newPermissions, callerPermissions);

        // 2. Role Aggregate'ine kendini güncellemesini söyle
        role.updateRole(newName, newPermissions);

        // 3. Olayı oluştur ve dön
        return new RoleUpdatedEvent(role, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public RoleDeletedEvent validateAndInitiateRoleDelete(Role role) {
        // 1. Silinmeye uygun mu kontrol et (Örn: Static rol silinemez)
        role.validateDelete();

        // 2. Olayı oluştur ve dön
        return new RoleDeletedEvent(role, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    /**
     * Döküman Madde 3.b.4 - Alt Küme Kuralı (Subset Rule)
     * İstek atan kullanıcı (Caller), oluşturacağı/güncelleyeceği role yalnızca kendi sahip olduğu yetkileri ekleyebilir.
     */
    private void validateSubsetRule(List<Permission> newPermissions, List<Permission> callerPermissions) {
        if (newPermissions == null || newPermissions.isEmpty()) {
            return;
        }
        if (callerPermissions == null || callerPermissions.isEmpty()) {
            throw new IdentityDomainException("Caller has no permissions to assign! Privilege Escalation attempt blocked.");
        }

        List<com.berkay.identity.service.domain.valueobject.PermissionId> callerPermissionIds = callerPermissions.stream()
                .map(Permission::getId)
                .toList();

        boolean isSubset = newPermissions.stream()
                .allMatch(permission -> callerPermissionIds.contains(permission.getId()));

        if (!isSubset) {
            throw new IdentityDomainException("Kendi Yetkisi Kadar Güçlü kuralı ihlali! Kendinizde olmayan bir yetkiyi başkasına atayamazsınız.");
        }
    }

    public void initiateCustomer(User user) {
        user.initializeCustomer();
    }

    public void initiateMerchant(User user) {
        user.initializeMerchant();
    }

    public void initiateInternalUser(User user) {
        user.initializeInternalUser();
    }

    @Override
    public void validateAndInitiatePermissionUpdate(Permission permission, String newDescription, boolean active) {
        permission.updatePermission(newDescription, active);
    }
}