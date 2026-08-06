package com.berkay.identity.service.handler.user;

import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.IntentId;
import com.berkay.identity.service.domain.valueobject.PermissionId;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.UnassignUserRoleCommand;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnassignUserRoleCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserUpdateIntentHelper userUpdateIntentHelper;

    @Mock
    private TokenRevocationPort tokenRevocationPort;

    @Mock
    private IdentityProviderPort identityProviderPort;

    @InjectMocks
    private UnassignUserRoleCommandHandler handler;

    private UUID userId;
    private UUID roleId;
    private UUID requesterId;
    private UUID merchantOrgUnitId;
    private User targetGlobalUser;
    private User targetRestaurantUser;
    private Role globalRole;
    private Role restaurantRole;
    private Permission assignPermission;
    private Role requesterGlobalRole;
    private Role requesterRestaurantRole;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        requesterId = UUID.randomUUID();
        merchantOrgUnitId = UUID.randomUUID();

        assignPermission = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_assign_role")
                .active(true)
                .build();

        requesterGlobalRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .permissions(List.of(assignPermission))
                .build();

        requesterRestaurantRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .organizationalUnitId(merchantOrgUnitId)
                .permissions(List.of(assignPermission))
                .build();

        globalRole = Role.builder()
                .roleId(new RoleId(roleId))
                .build();

        restaurantRole = Role.builder()
                .roleId(new RoleId(roleId))
                .organizationalUnitId(merchantOrgUnitId)
                .build();

        targetGlobalUser = User.builder()
                .userId(new UserId(userId))
                .userType(com.berkay.identity.service.domain.valueobject.UserType.INTERNAL)
                .roles(new java.util.ArrayList<>(List.of(globalRole)))
                .build();

        targetRestaurantUser = User.builder()
                .userId(new UserId(userId))
                .userType(com.berkay.identity.service.domain.valueobject.UserType.MERCHANT)
                .organizationalUnitIds(List.of(merchantOrgUnitId))
                .roles(new java.util.ArrayList<>(List.of(restaurantRole)))
                .build();
    }

    @Test
    void shouldUnassignGlobalRoleSuccessfully_WhenAdmin() {
        UnassignUserRoleCommand command = new UnassignUserRoleCommand(
                userId, roleId, requesterId, com.berkay.identity.service.domain.valueobject.UserType.INTERNAL, List.of(requesterGlobalRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetGlobalUser));
        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(globalRole));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterGlobalRole));

        UserUpdateIntent mockIntent = UserUpdateIntent.builder()
                .intentId(new IntentId(UUID.randomUUID()))
                .build();
        when(userUpdateIntentHelper.createIntent(eq(userId), eq("UPDATE_USER"), anyString(), eq("")))
                .thenReturn(mockIntent);

        handler.unassign(command);

        verify(userUpdateIntentHelper).completeIntent(eq(mockIntent.getId().getValue()), any());
    }

    @Test
    void shouldUnassignRestaurantRoleSuccessfully_WhenMerchant() {
        UnassignUserRoleCommand command = new UnassignUserRoleCommand(
                userId, roleId, requesterId, com.berkay.identity.service.domain.valueobject.UserType.MERCHANT, List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetRestaurantUser));
        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(restaurantRole));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterRestaurantRole));

        UserUpdateIntent mockIntent = UserUpdateIntent.builder()
                .intentId(new IntentId(UUID.randomUUID()))
                .build();
        when(userUpdateIntentHelper.createIntent(eq(userId), eq("UPDATE_USER"), anyString(), eq("")))
                .thenReturn(mockIntent);

        handler.unassign(command);

        verify(userUpdateIntentHelper).completeIntent(eq(mockIntent.getId().getValue()), any());
    }

    @Test
    void shouldThrowException_WhenAdminTriesToUnassignRestaurantRole() {
        UnassignUserRoleCommand command = new UnassignUserRoleCommand(
                userId, roleId, requesterId, com.berkay.identity.service.domain.valueobject.UserType.INTERNAL, List.of(requesterGlobalRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetRestaurantUser));
        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(restaurantRole));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterGlobalRole));

        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.unassign(command));
        org.junit.jupiter.api.Assertions.assertEquals("Only MERCHANT users can unassign restaurant-specific roles! Admin cannot touch restaurant roles.", exception.getMessage());
        verify(userUpdateIntentHelper, never()).createIntent(any(), any(), any(), any());
    }

    @Test
    void shouldThrowException_WhenMerchantTriesToUnassignGlobalRole() {
        UnassignUserRoleCommand command = new UnassignUserRoleCommand(
                userId, roleId, requesterId, com.berkay.identity.service.domain.valueobject.UserType.MERCHANT, List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetRestaurantUser));
        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(globalRole));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterRestaurantRole));

        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.unassign(command));
        org.junit.jupiter.api.Assertions.assertEquals("Only INTERNAL users can unassign global roles!", exception.getMessage());
        verify(userUpdateIntentHelper, never()).createIntent(any(), any(), any(), any());
    }

    @Test
    void shouldThrowException_WhenUnassigningGlobalRoleFromNonInternalUser() {
        UnassignUserRoleCommand command = new UnassignUserRoleCommand(
                userId, roleId, requesterId, com.berkay.identity.service.domain.valueobject.UserType.INTERNAL, List.of(requesterGlobalRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetRestaurantUser)); // Not INTERNAL
        when(roleRepository.findById(new RoleId(roleId))).thenReturn(Optional.of(globalRole));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterGlobalRole));

        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.unassign(command));
        org.junit.jupiter.api.Assertions.assertEquals("Global roles can only be unassigned from INTERNAL users!", exception.getMessage());
        verify(userUpdateIntentHelper, never()).createIntent(any(), any(), any(), any());
    }
}
