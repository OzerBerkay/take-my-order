package com.berkay.identity.service.handler.role;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.event.RoleUpdatedEvent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.DomainType;
import com.berkay.identity.service.domain.valueobject.PermissionId;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.command.role.UpdateRoleCommand;
import com.berkay.identity.service.dto.command.role.UpdateRoleResponse;
import com.berkay.identity.service.mapper.RoleDataMapper;
import com.berkay.identity.service.outbox.helper.RoleOutboxHelper;
import com.berkay.identity.service.outbox.model.role.RoleEventPayload;
import com.berkay.identity.service.ports.output.repository.PermissionRepository;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.security.SecurityContextPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateRoleCommandHandlerTest {

    @Mock
    private IdentityDomainService identityDomainService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private SecurityContextPort securityContextPort;

    @Mock
    private RoleDataMapper roleDataMapper;

    @Mock
    private RoleOutboxHelper roleOutboxHelper;

    @Mock
    private com.berkay.identity.service.ports.output.repository.TokenRevocationPort tokenRevocationPort;

    @InjectMocks
    private UpdateRoleCommandHandler commandHandler;

    private UUID restaurantId;
    private UUID roleId;
    private UUID callerRoleId;
    private Role callerRole;
    private Permission permViewUsers;
    private Permission permManageRest;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        callerRoleId = UUID.randomUUID();

        permViewUsers = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_view_merchant_users")
                .description("View merchant users")
                .domain(DomainType.IDENTITY)
                .active(true)
                .isRestricted(false)
                .build();

        permManageRest = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_manage_restaurant")
                .description("Manage restaurant")
                .domain(DomainType.RESTAURANT)
                .active(true)
                .isRestricted(false)
                .build();

        callerRole = Role.builder()
                .roleId(new RoleId(callerRoleId))
                .name("RESTAURANT_OWNER")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(true)
                .build();
    }

    @Test
    @DisplayName("Başarılı Rol Güncelleme: Restoran sahibi rolü yeni izinlerle güncelleyebilir")
    void shouldSuccessfullyUpdateRole_WhenCommandIsValid() {
        UpdateRoleCommand command = UpdateRoleCommand.builder()
                .roleId(roleId)
                .name("Updated Supervisor")
                .organizationalUnitId(restaurantId)
                .permissionIds(Set.of(permViewUsers.getId().getValue(), permManageRest.getId().getValue()))
                .build();

        Role existingRole = Role.builder()
                .roleId(new RoleId(roleId))
                .name("Old Supervisor")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(false)
                .createdByUserId(new UserId(UUID.randomUUID()))
                .permissions(List.of(permViewUsers))
                .build();

        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(existingRole));
        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.MERCHANT);
        when(securityContextPort.getCurrentUserRoleIds()).thenReturn(List.of(callerRoleId));
        when(roleRepository.findAllById(List.of(callerRoleId))).thenReturn(List.of(callerRole));

        when(roleRepository.existsByNameAndOrganizationalUnitIdAndIdNot("Updated Supervisor", restaurantId, roleId))
                .thenReturn(false);

        when(permissionRepository.findActivePermissionsByIds(command.getPermissionIds()))
                .thenReturn(List.of(permViewUsers, permManageRest));

        when(permissionRepository.findActivePermissionsByRoleIds(List.of(callerRoleId)))
                .thenReturn(List.of(permViewUsers, permManageRest));

        when(identityDomainService.validateAndInitiateRoleUpdate(any(), any(), anyList(), anyList()))
                .thenReturn(new RoleUpdatedEvent(existingRole, ZonedDateTime.now(ZoneId.of("UTC"))));

        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);
        when(roleDataMapper.roleUpdatedEventToRoleEventPayload(any(Role.class)))
                .thenReturn(mock(RoleEventPayload.class));

        UpdateRoleResponse response = commandHandler.updateRole(command);

        assertNotNull(response);
        assertEquals(roleId, response.getRoleId());
        verify(roleRepository).save(any(Role.class));
        verify(roleOutboxHelper).saveRoleOutboxMessage(any());
        verify(tokenRevocationPort).revokeRole(roleId);
    }

    @Test
    @DisplayName("Statik Rol Dokunulmazlığı: Statik rol güncellenmek istendiğinde hata fırlatılır")
    void shouldThrowException_WhenRoleIsStatic() {
        UpdateRoleCommand command = UpdateRoleCommand.builder()
                .roleId(roleId)
                .name("Updated Static Role")
                .permissionIds(Set.of(permViewUsers.getId().getValue()))
                .build();

        Role staticRole = Role.builder()
                .roleId(new RoleId(roleId))
                .name("RESTAURANT_OWNER")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(true)
                .build();

        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(staticRole));

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.updateRole(command)
        );

        assertTrue(ex.getMessage().contains("Static roles (is_static=true) cannot be updated"));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("IDOR Engeli: Restoran yetkilisi başka bir restorana ait rolü güncelleyemez")
    void shouldThrowException_WhenMerchantTriesToUpdateRoleOfAnotherRestaurant() {
        UUID otherRestaurantId = UUID.randomUUID();
        UpdateRoleCommand command = UpdateRoleCommand.builder()
                .roleId(roleId)
                .name("Hacked Role")
                .organizationalUnitId(otherRestaurantId)
                .permissionIds(Set.of(permViewUsers.getId().getValue()))
                .build();

        Role existingRoleOfOtherRestaurant = Role.builder()
                .roleId(new RoleId(roleId))
                .name("Role In Other Restaurant")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(otherRestaurantId)
                .isStatic(false)
                .build();

        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(existingRoleOfOtherRestaurant));
        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.MERCHANT);
        when(securityContextPort.getCurrentUserRoleIds()).thenReturn(List.of(callerRoleId));
        when(roleRepository.findAllById(List.of(callerRoleId))).thenReturn(List.of(callerRole)); // caller is in restaurantId

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.updateRole(command)
        );

        assertTrue(ex.getMessage().contains("Spoofing detected"));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kısıtlı İzin Engeli: Özel role is_restricted=true olan yetki verilemez")
    void shouldThrowException_WhenUpdatingWithRestrictedPermission() {
        Permission restrictedPerm = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_create_role")
                .description("Create role")
                .domain(DomainType.IDENTITY)
                .active(true)
                .isRestricted(true)
                .build();

        UpdateRoleCommand command = UpdateRoleCommand.builder()
                .roleId(roleId)
                .name("Supervisor")
                .organizationalUnitId(restaurantId)
                .permissionIds(Set.of(restrictedPerm.getId().getValue()))
                .build();

        Role existingRole = Role.builder()
                .roleId(new RoleId(roleId))
                .name("Supervisor")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(false)
                .build();

        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(existingRole));
        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.MERCHANT);
        when(securityContextPort.getCurrentUserRoleIds()).thenReturn(List.of(callerRoleId));
        when(roleRepository.findAllById(List.of(callerRoleId))).thenReturn(List.of(callerRole));
        when(roleRepository.existsByNameAndOrganizationalUnitIdAndIdNot("Supervisor", restaurantId, roleId))
                .thenReturn(false);
        when(permissionRepository.findActivePermissionsByIds(command.getPermissionIds()))
                .thenReturn(List.of(restrictedPerm));

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.updateRole(command)
        );

        assertTrue(ex.getMessage().contains("Cannot assign restricted permissions to a custom role"));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("İç Kullanıcı İzolasyonu: INTERNAL kullanıcı restoran bazlı rolü güncelleyemez")
    void shouldThrowException_WhenInternalUserTriesToUpdateRestaurantRole() {
        UpdateRoleCommand command = UpdateRoleCommand.builder()
                .roleId(roleId)
                .name("Internal Attempt")
                .permissionIds(Set.of(permViewUsers.getId().getValue()))
                .build();

        Role existingRestaurantRole = Role.builder()
                .roleId(new RoleId(roleId))
                .name("Restaurant Custom Role")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(false)
                .build();

        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(existingRestaurantRole));
        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.INTERNAL);

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.updateRole(command)
        );

        assertTrue(ex.getMessage().contains("Internal users can only manage global roles"));
        verify(roleRepository, never()).save(any());
    }
}
