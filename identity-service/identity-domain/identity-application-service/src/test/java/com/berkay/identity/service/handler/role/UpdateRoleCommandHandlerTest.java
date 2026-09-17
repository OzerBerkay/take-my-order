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

    @InjectMocks
    private UpdateRoleCommandHandler commandHandler;

    private UUID restaurantId;
    private UUID roleId;
    private Permission permViewUsers;
    private Permission permManageRest;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        roleId = UUID.randomUUID();

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

        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.MERCHANT);
        when(securityContextPort.getAllowedOrganizationalUnitIds()).thenReturn(Set.of(restaurantId));

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
        when(roleRepository.existsByNameAndOrganizationalUnitIdAndIdNot("Updated Supervisor", restaurantId, roleId))
                .thenReturn(false);

        when(permissionRepository.findActivePermissionsByIds(command.getPermissionIds()))
                .thenReturn(List.of(permViewUsers, permManageRest));

        UUID userRoleUuid = UUID.randomUUID();
        when(securityContextPort.getCurrentUserRoleIds()).thenReturn(List.of(userRoleUuid));
        when(permissionRepository.findActivePermissionsByRoleIds(List.of(userRoleUuid)))
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
    }

    @Test
    @DisplayName("IDOR Engeli: Restoran sahibi başka bir restorana ait rolü güncelleyemez")
    void shouldThrowException_WhenMerchantTriesToUpdateRoleOfAnotherRestaurant() {
        UUID otherRestaurantId = UUID.randomUUID();
        UpdateRoleCommand command = UpdateRoleCommand.builder()
                .roleId(roleId)
                .name("Hacked Role")
                .organizationalUnitId(otherRestaurantId)
                .permissionIds(Set.of(permViewUsers.getId().getValue()))
                .build();

        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.MERCHANT);
        when(securityContextPort.getAllowedOrganizationalUnitIds()).thenReturn(Set.of(restaurantId));

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.updateRole(command)
        );

        assertTrue(ex.getMessage().contains("Spoofing detected"));
        verify(roleRepository, never()).save(any());
    }
}
