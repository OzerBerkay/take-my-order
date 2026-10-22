package com.berkay.identity.service.application.security.auth;

import com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.valueobject.DomainType;
import com.berkay.identity.service.domain.valueobject.PermissionId;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleAuthorizationServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleAuthorizationService roleAuthService;

    private JwtAuthenticationToken m2mAuth;
    private JwtAuthenticationToken customerAuth;
    private JwtAuthenticationToken internalAuth;
    private JwtAuthenticationToken merchantAuth;

    private UUID restaurantA;
    private UUID restaurantB;
    private UUID ownerRoleIdA;
    private UUID waiterRoleIdB;
    private UUID sysAdminRoleId;
    private UUID dynamicRoleIdA;

    private Role ownerRoleA;
    private Role waiterRoleB;
    private Role sysAdminRole;
    private Role dynamicRoleA;

    @BeforeEach
    void setUp() {
        m2mAuth = mock(JwtAuthenticationToken.class);
        customerAuth = mock(JwtAuthenticationToken.class);
        internalAuth = mock(JwtAuthenticationToken.class);
        merchantAuth = mock(JwtAuthenticationToken.class);

        restaurantA = UUID.randomUUID();
        restaurantB = UUID.randomUUID();
        ownerRoleIdA = UUID.randomUUID();
        waiterRoleIdB = UUID.randomUUID();
        sysAdminRoleId = UUID.randomUUID();
        dynamicRoleIdA = UUID.randomUUID();

        Permission canCreateRole = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_create_role")
                .description("Create role")
                .domain(DomainType.IDENTITY)
                .active(true)
                .isRestricted(true)
                .build();

        Permission canUpdateRole = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_update_role")
                .description("Update role")
                .domain(DomainType.IDENTITY)
                .active(true)
                .isRestricted(true)
                .build();

        Permission canDeleteRole = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_delete_role")
                .description("Delete role")
                .domain(DomainType.IDENTITY)
                .active(true)
                .isRestricted(true)
                .build();

        Permission canViewOrders = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_view_orders")
                .description("View orders")
                .domain(DomainType.ORDER)
                .active(true)
                .isRestricted(false)
                .build();

        // RESTAURANT_OWNER in Restaurant A (Static = true)
        ownerRoleA = Role.builder()
                .roleId(new RoleId(ownerRoleIdA))
                .name("RESTAURANT_OWNER")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantA)
                .isStatic(true)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(canCreateRole, canUpdateRole, canDeleteRole))
                .build();

        // WAITER in Restaurant B (Static = false)
        waiterRoleB = Role.builder()
                .roleId(new RoleId(waiterRoleIdB))
                .name("WAITER")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantB)
                .isStatic(false)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(canViewOrders))
                .build();

        // SYSTEM_ADMIN (Static = true, Global)
        sysAdminRole = Role.builder()
                .roleId(new RoleId(sysAdminRoleId))
                .name("SYSTEM_ADMIN")
                .userType(UserType.INTERNAL)
                .organizationalUnitId(null)
                .isStatic(true)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(canCreateRole, canUpdateRole, canDeleteRole))
                .build();

        // Dynamic Role in Restaurant A (Static = false)
        dynamicRoleA = Role.builder()
                .roleId(new RoleId(dynamicRoleIdA))
                .name("SHIFT_LEAD")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantA)
                .isStatic(false)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(canViewOrders))
                .build();
    }

    @Test
    @DisplayName("M2M yetkilendirmesi doğrudan onaylanmalıdır")
    void hasPermission_ShouldReturnTrue_WhenUserTypeIsM2M() {
        when(m2mAuth.getUserType()).thenReturn(UserType.M2M);
        assertTrue(roleAuthService.hasPermission(m2mAuth, "can_create_role"));
        verifyNoInteractions(roleRepository);
    }

    @Test
    @DisplayName("CUSTOMER yetkilendirmesi her zaman reddedilmelidir")
    void hasPermission_ShouldReturnFalse_WhenUserTypeIsCustomer() {
        when(customerAuth.getUserType()).thenReturn(UserType.CUSTOMER);
        assertFalse(roleAuthService.canCreateRole(customerAuth, restaurantA));
    }

    @Test
    @DisplayName("Merchant kendi restoranında can_create_role yetkisine sahipse başarılı olmalıdır")
    void canCreateRole_ShouldReturnTrue_WhenMerchantHasRoleInTargetRestaurant() {
        when(merchantAuth.getUserType()).thenReturn(UserType.MERCHANT);
        when(merchantAuth.getRoleIds()).thenReturn(List.of(ownerRoleIdA));
        when(roleRepository.findById(new RoleId(ownerRoleIdA))).thenReturn(Optional.of(ownerRoleA));

        boolean result = roleAuthService.canCreateRole(merchantAuth, restaurantA);
        assertTrue(result);
    }

    @Test
    @DisplayName("Çapraz Restoran Koruması: Restoran A'nın sahibi, Restoran B'de rol yaratamaz")
    void canCreateRole_ShouldReturnFalse_WhenMerchantAttemptsToCreateInDifferentRestaurant() {
        // Merchant has owner role in A, and waiter role in B
        when(merchantAuth.getUserType()).thenReturn(UserType.MERCHANT);
        when(merchantAuth.getRoleIds()).thenReturn(List.of(ownerRoleIdA, waiterRoleIdB));
        when(roleRepository.findById(new RoleId(ownerRoleIdA))).thenReturn(Optional.of(ownerRoleA));
        when(roleRepository.findById(new RoleId(waiterRoleIdB))).thenReturn(Optional.of(waiterRoleB));

        // Attempting to create role in Restaurant B where he is only a waiter
        boolean result = roleAuthService.canCreateRole(merchantAuth, restaurantB);
        assertFalse(result, "Restoran A'nın patronluk yetkisi Restoran B'ye asla sızmamalıdır!");
    }

    @Test
    @DisplayName("Internal kullanıcı global rol yaratırken onay almalıdır")
    void canCreateRole_ShouldReturnTrue_WhenInternalUserCreatesGlobalRole() {
        when(internalAuth.getUserType()).thenReturn(UserType.INTERNAL);
        when(internalAuth.getRoleIds()).thenReturn(List.of(sysAdminRoleId));
        when(roleRepository.findById(new RoleId(sysAdminRoleId))).thenReturn(Optional.of(sysAdminRole));

        boolean result = roleAuthService.canCreateRole(internalAuth, null);
        assertTrue(result);
    }

    @Test
    @DisplayName("Internal kullanıcı restoran context'i verirse rol yaratamaz")
    void canCreateRole_ShouldReturnFalse_WhenInternalUserSpecifiesRestaurantContext() {
        when(internalAuth.getUserType()).thenReturn(UserType.INTERNAL);
        boolean result = roleAuthService.canCreateRole(internalAuth, restaurantA);
        assertFalse(result, "Internal kullanıcı restoran context'inde rol yaratamaz!");
    }

    @Test
    @DisplayName("Statik Rol Güncelleme Engeli: Statik roller asla güncellenemez (403)")
    void canUpdateRole_ShouldReturnFalse_WhenTargetRoleIsStatic() {
        when(roleRepository.findById(new RoleId(ownerRoleIdA))).thenReturn(Optional.of(ownerRoleA));

        boolean result = roleAuthService.canUpdateRole(merchantAuth, ownerRoleIdA);
        assertFalse(result, "Statik roller (is_static=true) asla güncellenemez!");
    }

    @Test
    @DisplayName("Statik Rol Silme Engeli: Statik roller asla silinemez (403)")
    void canDeleteRole_ShouldReturnFalse_WhenTargetRoleIsStatic() {
        when(roleRepository.findById(new RoleId(ownerRoleIdA))).thenReturn(Optional.of(ownerRoleA));

        boolean result = roleAuthService.canDeleteRole(merchantAuth, ownerRoleIdA);
        assertFalse(result, "Statik roller (is_static=true) asla silinemez!");
    }

    @Test
    @DisplayName("Dinamik Rol Güncelleme: Restoran sahibi restoranındaki dinamik rolü güncelleyebilir")
    void canUpdateRole_ShouldReturnTrue_WhenTargetRoleIsDynamicAndCallerIsOwner() {
        when(roleRepository.findById(new RoleId(dynamicRoleIdA))).thenReturn(Optional.of(dynamicRoleA));
        when(merchantAuth.getUserType()).thenReturn(UserType.MERCHANT);
        when(merchantAuth.getRoleIds()).thenReturn(List.of(ownerRoleIdA));
        when(roleRepository.findById(new RoleId(ownerRoleIdA))).thenReturn(Optional.of(ownerRoleA));

        boolean result = roleAuthService.canUpdateRole(merchantAuth, dynamicRoleIdA);
        assertTrue(result);
    }

    @Test
    @DisplayName("Dinamik Rol Silme: Restoran sahibi restoranındaki dinamik rolü silebilir")
    void canDeleteRole_ShouldReturnTrue_WhenTargetRoleIsDynamicAndCallerIsOwner() {
        when(roleRepository.findById(new RoleId(dynamicRoleIdA))).thenReturn(Optional.of(dynamicRoleA));
        when(merchantAuth.getUserType()).thenReturn(UserType.MERCHANT);
        when(merchantAuth.getRoleIds()).thenReturn(List.of(ownerRoleIdA));
        when(roleRepository.findById(new RoleId(ownerRoleIdA))).thenReturn(Optional.of(ownerRoleA));

        boolean result = roleAuthService.canDeleteRole(merchantAuth, dynamicRoleIdA);
        assertTrue(result);
    }
}
