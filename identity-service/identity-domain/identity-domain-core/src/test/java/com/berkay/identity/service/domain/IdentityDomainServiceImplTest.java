package com.berkay.identity.service.domain;

import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.event.RoleCreatedEvent;
import com.berkay.identity.service.domain.event.RoleUpdatedEvent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.DomainType;
import com.berkay.identity.service.domain.valueobject.PermissionId;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IdentityDomainServiceImplTest {

    private IdentityDomainService identityDomainService;

    private Permission perm1;
    private Permission perm2;
    private Permission permUnauthorized;
    private Permission permRestricted;

    @BeforeEach
    void setUp() {
        identityDomainService = new IdentityDomainServiceImpl();

        perm1 = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_view_merchant_users")
                .description("View merchant users")
                .domain(DomainType.IDENTITY)
                .active(true)
                .isRestricted(false)
                .build();

        perm2 = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_manage_restaurant")
                .description("Manage restaurant")
                .domain(DomainType.RESTAURANT)
                .active(true)
                .isRestricted(false)
                .build();

        permUnauthorized = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_manage_system_settings")
                .description("Manage system settings")
                .domain(DomainType.IDENTITY)
                .active(true)
                .isRestricted(false)
                .build();

        permRestricted = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_assign_role")
                .description("Assign role")
                .domain(DomainType.IDENTITY)
                .active(true)
                .isRestricted(true)
                .build();
    }

    @Test
    @DisplayName("Başarılı Rol Oluşturma: Caller'ın sahip olduğu yetkilerle yeni bir rol oluşturulabilir")
    void shouldCreateRole_WhenCallerHasAllPermissions() {
        UUID restaurantId = UUID.randomUUID();
        Role role = Role.builder()
                .name("Assistant Manager")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(false)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(perm1, perm2))
                .build();

        List<Permission> callerPermissions = List.of(perm1, perm2, permUnauthorized);

        RoleCreatedEvent event = identityDomainService.validateAndInitiateRoleCreate(role, callerPermissions);

        assertNotNull(event);
        assertNotNull(event.getRole().getId());
        assertEquals("Assistant Manager", event.getRole().getName());
        assertEquals(2, event.getRole().getPermissions().size());
    }

    @Test
    @DisplayName("Yetki Yükseltme Engeli (Subset Rule): Caller'da olmayan yetki role eklenemez")
    void shouldThrowException_WhenCallerLacksPermission() {
        UUID restaurantId = UUID.randomUUID();
        Role role = Role.builder()
                .name("Hacker Role")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(false)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(perm1, permUnauthorized))
                .build();

        // Caller only has perm1, but tries to grant permUnauthorized
        List<Permission> callerPermissions = List.of(perm1);

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                identityDomainService.validateAndInitiateRoleCreate(role, callerPermissions)
        );

        assertTrue(ex.getMessage().contains("Kendi Yetkisi Kadar Güçlü kuralı ihlali"));
    }

    @Test
    @DisplayName("Dikey Yetki Yükseltme Engeli: Dinamik role restricted permission eklenemez")
    void shouldThrowException_WhenAssigningRestrictedPermissionToDynamicRole() {
        UUID restaurantId = UUID.randomUUID();
        Role role = Role.builder()
                .name("Custom Admin")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(false)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(permRestricted))
                .build();

        List<Permission> callerPermissions = List.of(permRestricted);

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                identityDomainService.validateAndInitiateRoleCreate(role, callerPermissions)
        );

        assertTrue(ex.getMessage().contains("Cannot assign restricted permissions to any role manually"));
    }

    @Test
    @DisplayName("Başarılı Rol Güncelleme: Caller'ın sahip olduğu yetkilerle rol güncellenebilir")
    void shouldUpdateRole_WhenPermissionsAreValid() {
        UUID restaurantId = UUID.randomUUID();
        Role role = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .name("Old Manager")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(false)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(perm1))
                .build();

        List<Permission> newPermissions = List.of(perm1, perm2);
        List<Permission> callerPermissions = List.of(perm1, perm2);

        RoleUpdatedEvent event = identityDomainService.validateAndInitiateRoleUpdate(role, "New Manager", newPermissions, callerPermissions);

        assertNotNull(event);
        assertEquals("New Manager", role.getName());
        assertEquals(2, role.getPermissions().size());
    }

    @Test
    @DisplayName("Statik Rol Güncelleme Engeli: Statik roller güncellenemez")
    void shouldThrowException_WhenUpdatingStaticRole() {
        Role staticRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .name("SYSTEM_ADMIN")
                .userType(UserType.INTERNAL)
                .isStatic(true)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(perm1))
                .build();

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                identityDomainService.validateAndInitiateRoleUpdate(staticRole, "NEW_ADMIN", List.of(perm1), List.of(perm1))
        );

        assertTrue(ex.getMessage().contains("Static roles (is_static=true) cannot be updated"));
    }

    @Test
    @DisplayName("Statik Rol Silme Engeli: Statik roller silinemez")
    void shouldThrowException_WhenDeletingStaticRole() {
        Role staticRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .name("RESTAURANT_OWNER")
                .userType(UserType.MERCHANT)
                .isStatic(true)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(perm1))
                .build();

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                identityDomainService.validateAndInitiateRoleDelete(staticRole)
        );

        assertTrue(ex.getMessage().contains("Static roles (is_static=true) cannot be deleted"));
    }
}
