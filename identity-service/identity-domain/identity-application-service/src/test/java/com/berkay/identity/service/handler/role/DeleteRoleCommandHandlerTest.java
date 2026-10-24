package com.berkay.identity.service.handler.role;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.event.RoleDeletedEvent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.command.role.DeleteRoleCommand;
import com.berkay.identity.service.dto.command.role.DeleteRoleResponse;
import com.berkay.identity.service.mapper.RoleDataMapper;
import com.berkay.identity.service.outbox.helper.RoleOutboxHelper;
import com.berkay.identity.service.outbox.model.role.RoleEventPayload;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteRoleCommandHandlerTest {

    @Mock
    private IdentityDomainService identityDomainService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleDataMapper roleDataMapper;

    @Mock
    private RoleOutboxHelper roleOutboxHelper;

    @Mock
    private SecurityContextPort securityContextPort;

    @Mock
    private com.berkay.identity.service.ports.output.repository.TokenRevocationPort tokenRevocationPort;

    @InjectMocks
    private DeleteRoleCommandHandler commandHandler;

    private UUID restaurantId;
    private UUID roleId;
    private UUID callerRoleId;
    private Role callerRole;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        roleId = UUID.randomUUID();
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
    @DisplayName("Başarılı Rol Silme: Restoran sahibi restoranına ait özel rolü silebilir")
    void shouldSuccessfullyDeleteRole_WhenMerchantCallerHasRoleInSameRestaurant() {
        DeleteRoleCommand command = DeleteRoleCommand.builder()
                .roleId(roleId)
                .organizationalUnitId(restaurantId)
                .build();

        Role existingRole = Role.builder()
                .roleId(new RoleId(roleId))
                .name("Custom Staff")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(false)
                .build();

        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(existingRole));
        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.MERCHANT);
        when(securityContextPort.getCurrentUserRoleIds()).thenReturn(List.of(callerRoleId));
        when(roleRepository.findAllById(List.of(callerRoleId))).thenReturn(List.of(callerRole));

        when(identityDomainService.validateAndInitiateRoleDelete(existingRole))
                .thenReturn(new RoleDeletedEvent(existingRole, ZonedDateTime.now(ZoneId.of("UTC"))));
        when(roleDataMapper.roleDeletedEventToRoleEventPayload(existingRole))
                .thenReturn(mock(RoleEventPayload.class));

        DeleteRoleResponse response = commandHandler.deleteRole(command);

        assertNotNull(response);
        assertEquals(roleId, response.getRoleId());
        verify(roleRepository).delete(existingRole);
        verify(roleOutboxHelper).saveRoleOutboxMessage(any());
        verify(tokenRevocationPort).revokeRole(roleId);
    }

    @Test
    @DisplayName("Statik Rol Dokunulmazlığı: Statik rol silinmek istendiğinde fail-fast hata fırlatılır")
    void shouldThrowException_WhenRoleIsStatic() {
        DeleteRoleCommand command = DeleteRoleCommand.builder()
                .roleId(roleId)
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
                commandHandler.deleteRole(command)
        );

        assertTrue(ex.getMessage().contains("Static roles (is_static=true) cannot be deleted"));
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("IDOR Engeli: Restoran yetkilisi başka restorana ait rolü silemez")
    void shouldThrowException_WhenMerchantTriesToDeleteRoleFromAnotherRestaurant() {
        UUID otherRestaurantId = UUID.randomUUID();
        DeleteRoleCommand command = DeleteRoleCommand.builder()
                .roleId(roleId)
                .build();

        Role existingRole = Role.builder()
                .roleId(new RoleId(roleId))
                .name("Other Custom Role")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(otherRestaurantId)
                .isStatic(false)
                .build();

        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(existingRole));
        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.MERCHANT);
        when(securityContextPort.getCurrentUserRoleIds()).thenReturn(List.of(callerRoleId));
        when(roleRepository.findAllById(List.of(callerRoleId))).thenReturn(List.of(callerRole)); // caller has restaurantId

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.deleteRole(command)
        );

        assertTrue(ex.getMessage().contains("Spoofing detected"));
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("İç Kullanıcı İzolasyonu: INTERNAL kullanıcı restoran bazlı rolü silemez")
    void shouldThrowException_WhenInternalUserTriesToDeleteRestaurantRole() {
        DeleteRoleCommand command = DeleteRoleCommand.builder()
                .roleId(roleId)
                .build();

        Role restaurantRole = Role.builder()
                .roleId(new RoleId(roleId))
                .name("Restaurant Custom Role")
                .userType(UserType.MERCHANT)
                .organizationalUnitId(restaurantId)
                .isStatic(false)
                .build();

        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(restaurantRole));
        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.INTERNAL);

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.deleteRole(command)
        );

        assertTrue(ex.getMessage().contains("Internal users can only delete global roles"));
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Müşteri Engeli: CUSTOMER rol silemez")
    void shouldThrowException_WhenCustomerTriesToDeleteRole() {
        DeleteRoleCommand command = DeleteRoleCommand.builder()
                .roleId(roleId)
                .build();

        Role globalRole = Role.builder()
                .roleId(new RoleId(roleId))
                .name("Custom Role")
                .userType(UserType.INTERNAL)
                .organizationalUnitId(null)
                .isStatic(false)
                .build();

        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(globalRole));
        when(securityContextPort.getCurrentUserType()).thenReturn(UserType.CUSTOMER);

        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () ->
                commandHandler.deleteRole(command)
        );

        assertTrue(ex.getMessage().contains("Customers are not allowed to delete roles"));
        verify(roleRepository, never()).delete(any());
    }
}
