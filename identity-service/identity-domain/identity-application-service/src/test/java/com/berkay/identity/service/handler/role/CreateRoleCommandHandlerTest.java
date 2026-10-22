package com.berkay.identity.service.handler.role;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.event.RoleCreatedEvent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.DomainType;
import com.berkay.identity.service.domain.valueobject.PermissionId;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.command.role.CreateRoleCommand;
import com.berkay.identity.service.dto.command.role.CreateRoleResponse;
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
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRoleCommandHandlerTest {

    @Mock
    private IdentityDomainService identityDomainService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private SecurityContextPort securityContextPort;

    @Mock
    private RoleOutboxHelper roleOutboxHelper;

    @Mock
    private RoleDataMapper roleDataMapper;

    @InjectMocks
    private CreateRoleCommandHandler commandHandler;

    private UUID restaurantId;
    private UUID callerUserId;
    private UUID permViewUsersId;
    private UUID permManageRestId;
    private Permission permViewUsers;
    private Permission permManageRest;
    private UUID callerRoleId;
    private Role callerRole;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        callerUserId = UUID.randomUUID();
        permViewUsersId = UUID.randomUUID();
        permManageRestId = UUID.randomUUID();

        permViewUsers = Permission.builder()
                .permissionId(new PermissionId(permViewUsersId))
                .code("can_view_merchant_users")
                .description("View merchant users")
                .domain(DomainType.IDENTITY)
                .active(true)
                .isRestricted(false)
                .build();

        permManageRest = Permission.builder()
                .permissionId(new PermissionId(permManageRestId))
                .code("can_manage_restaurant")
                .description("Manage restaurant")
                .domain(DomainType.RESTAURANT)
                .active(true)
                .isRestricted(false)
                .build();

        callerRoleId = UUID.randomUUID();
        callerRole = Role.builder()
                .roleId(new RoleId(callerRoleId))
                .name("RESTAURANT_OWNER")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(true)
                .build();
    }

    @Test
    @DisplayName("Başarılı Özel Rol Oluşturma: Restoran yetkilisi geçerli izinlerle özel rol oluşturabilir")
    void shouldSuccessfullyCreateCustomRole_WhenMerchantAssignsValidPermissions() {
        CreateRoleCommand command = CreateRoleCommand.builder()
                .name("Shift Supervisor")
                .organizationalUnitId(restaurantId)
                .permissionIds(Set.of(permViewUsersId, permManageRestId))
                .build();

        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.MERCHANT);
        when(securityContextPort.getCurrentInternalUserId()).thenReturn(callerUserId);
        when(securityContextPort.getCurrentUserRoleIds()).thenReturn(List.of(callerRoleId));
        when(roleRepository.findAllById(List.of(callerRoleId))).thenReturn(List.of(callerRole));
        when(roleRepository.existsByNameAndOrganizationalUnitId("Shift Supervisor", restaurantId)).thenReturn(false);

        when(permissionRepository.findActivePermissionsByIds(Set.of(permViewUsersId, permManageRestId)))
                .thenReturn(List.of(permViewUsers, permManageRest));

        when(permissionRepository.findActivePermissionsByRoleIds(List.of(callerRoleId)))
                .thenReturn(List.of(permViewUsers, permManageRest));

        Role savedRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .name("Shift Supervisor")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(false)
                .createdByUserId(new UserId(callerUserId))
                .permissions(List.of(permViewUsers, permManageRest))
                .build();

        when(identityDomainService.validateAndInitiateRoleCreate(any(Role.class), anyList()))
                .thenReturn(new RoleCreatedEvent(savedRole, ZonedDateTime.now(ZoneId.of("UTC"))));

        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);
        when(roleDataMapper.roleCreatedEventToRoleEventPayload(any(Role.class)))
                .thenReturn(mock(RoleEventPayload.class));

        CreateRoleResponse response = commandHandler.createRole(command);

        assertNotNull(response);
        assertEquals(savedRole.getId().getValue(), response.getRoleId());
        verify(roleRepository).save(any(Role.class));
        verify(roleOutboxHelper).saveRoleOutboxMessage(any());
    }

    @Test
    @DisplayName("IDOR Engeli: Kullanıcı yetkili olmadığı başka bir restorana rol açmaya çalışırsa Spoofing hatası alır")
    void shouldThrowException_WhenMerchantTriesToCreateRoleForAnotherRestaurant() {
        UUID otherRestaurantId = UUID.randomUUID();
        CreateRoleCommand command = CreateRoleCommand.builder()
                .name("Hacked Role")
                .organizationalUnitId(otherRestaurantId)
                .permissionIds(Set.of(permViewUsersId))
                .build();

        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.MERCHANT);
        when(securityContextPort.getCurrentInternalUserId()).thenReturn(callerUserId);
        when(securityContextPort.getCurrentUserRoleIds()).thenReturn(List.of(callerRoleId));
        when(roleRepository.findAllById(List.of(callerRoleId))).thenReturn(List.of(callerRole));

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.createRole(command)
        );

        assertTrue(ex.getMessage().contains("Spoofing detected"));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dikey Yetki Yükseltme Engeli: is_restricted=true olan yetki dinamik role atanamaz")
    void shouldThrowException_WhenAssigningRestrictedPermission() {
        Permission restrictedPerm = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_reset_password")
                .description("Reset password")
                .domain(DomainType.IDENTITY)
                .active(true)
                .isRestricted(true)
                .build();

        CreateRoleCommand command = CreateRoleCommand.builder()
                .name("Super Role")
                .organizationalUnitId(restaurantId)
                .permissionIds(Set.of(restrictedPerm.getId().getValue()))
                .build();

        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.MERCHANT);
        when(securityContextPort.getCurrentInternalUserId()).thenReturn(callerUserId);
        when(securityContextPort.getCurrentUserRoleIds()).thenReturn(List.of(callerRoleId));
        when(roleRepository.findAllById(List.of(callerRoleId))).thenReturn(List.of(callerRole));
        when(roleRepository.existsByNameAndOrganizationalUnitId("Super Role", restaurantId)).thenReturn(false);
        when(permissionRepository.findActivePermissionsByIds(anySet()))
                .thenReturn(List.of(restrictedPerm));

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.createRole(command)
        );

        assertTrue(ex.getMessage().contains("Cannot assign restricted permissions to a custom role"));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("İç Kullanıcı İzolasyonu: INTERNAL kullanıcı restoran bazlı rol oluşturamaz")
    void shouldThrowException_WhenInternalUserSpecifiesOrganizationalUnitId() {
        CreateRoleCommand command = CreateRoleCommand.builder()
                .name("Internal Custom Role")
                .organizationalUnitId(restaurantId)
                .permissionIds(Set.of(permViewUsersId))
                .build();

        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.INTERNAL);
        when(securityContextPort.getCurrentInternalUserId()).thenReturn(callerUserId);

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.createRole(command)
        );

        assertTrue(ex.getMessage().contains("Internal users can only manage global roles"));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Müşteri Engeli: CUSTOMER kullanıcı rol oluşturamaz")
    void shouldThrowException_WhenCustomerTriesToCreateRole() {
        CreateRoleCommand command = CreateRoleCommand.builder()
                .name("Customer Role")
                .organizationalUnitId(null)
                .permissionIds(Set.of(permViewUsersId))
                .build();

        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.CUSTOMER);
        when(securityContextPort.getCurrentInternalUserId()).thenReturn(callerUserId);

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.createRole(command)
        );

        assertTrue(ex.getMessage().contains("Customers are not allowed to create roles"));
        verify(roleRepository, never()).save(any());
    }
}
