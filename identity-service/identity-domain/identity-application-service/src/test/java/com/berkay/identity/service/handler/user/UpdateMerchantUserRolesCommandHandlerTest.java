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
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.command.UpdateMerchantUserRolesCommand;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateMerchantUserRolesCommandHandlerTest {

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
    private UpdateMerchantUserRolesCommandHandler handler;

    private UUID userId;
    private UUID requesterId;
    private UUID merchantOrgUnitId;
    private Role requesterRestaurantRole;
    private Role restaurantRole1;
    private Role restaurantRole2;
    private User targetMerchantUser;
    private Permission assignPermission;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        requesterId = UUID.randomUUID();
        merchantOrgUnitId = UUID.randomUUID();

        assignPermission = Permission.builder()
                .permissionId(new PermissionId(UUID.randomUUID()))
                .code("can_assign_role")
                .active(true)
                .build();

        requesterRestaurantRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .organizationalUnitId(merchantOrgUnitId)
                .permissions(List.of(assignPermission))
                .build();

        restaurantRole1 = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .name("WAITER")
                .organizationalUnitId(merchantOrgUnitId)
                .isStatic(false)
                .build();

        restaurantRole2 = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .name("CHEF")
                .organizationalUnitId(merchantOrgUnitId)
                .isStatic(false)
                .build();

        targetMerchantUser = User.builder()
                .userId(new UserId(userId))
                .externalId("ext-user-123")
                .userType(UserType.MERCHANT)
                .organizationalUnitIds(List.of(merchantOrgUnitId))
                .roles(Collections.emptyList())
                .build();
    }

    @Test
    void shouldSuccessfullyUpdateRolesInBatch() {
        UUID role1Id = restaurantRole1.getId().getValue();
        UUID role2Id = restaurantRole2.getId().getValue();

        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                List.of(role1Id, role2Id),
                requesterId,
                UserType.MERCHANT,
                List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetMerchantUser));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterRestaurantRole));
        when(roleRepository.findAllById(List.of(role1Id, role2Id))).thenReturn(List.of(restaurantRole1, restaurantRole2));

        UserUpdateIntent mockIntent = mock(UserUpdateIntent.class);
        when(mockIntent.getId()).thenReturn(new IntentId(UUID.randomUUID()));
        when(userUpdateIntentHelper.createIntent(eq(userId), eq("UPDATE_USER"), anyString(), anyString()))
                .thenReturn(mockIntent);

        handler.update(command);

        verify(identityProviderPort).updateUserRolesAndBranches(
                eq(targetMerchantUser.getExternalId()),
                argThat(list -> list.contains(role1Id.toString()) && list.contains(role2Id.toString())),
                argThat(list -> list.contains(merchantOrgUnitId.toString()))
        );
        verify(userUpdateIntentHelper).markKeycloakDone(mockIntent.getId().getValue());
        verify(userUpdateIntentHelper).completeIntent(eq(mockIntent.getId().getValue()), any());
        verify(tokenRevocationPort).revokeAccessToken(userId);
    }

    @Test
    void shouldSuccessfullyClearRolesWhenEmptyListProvided() {
        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                Collections.emptyList(),
                requesterId,
                UserType.MERCHANT,
                List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetMerchantUser));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterRestaurantRole));

        UserUpdateIntent mockIntent = mock(UserUpdateIntent.class);
        when(mockIntent.getId()).thenReturn(new IntentId(UUID.randomUUID()));
        when(userUpdateIntentHelper.createIntent(eq(userId), eq("UPDATE_USER"), anyString(), anyString()))
                .thenReturn(mockIntent);

        handler.update(command);

        verify(roleRepository, never()).findAllById(argThat(list -> !list.equals(command.requesterRoleIds())));
        verify(identityProviderPort).updateUserRolesAndBranches(
                eq(targetMerchantUser.getExternalId()),
                eq(Collections.emptyList()),
                argThat(list -> list.contains(merchantOrgUnitId.toString()))
        );
        verify(userUpdateIntentHelper).completeIntent(eq(mockIntent.getId().getValue()), any());
        verify(tokenRevocationPort).revokeAccessToken(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                Collections.emptyList(),
                requesterId,
                UserType.MERCHANT,
                List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.empty());

        assertThrows(IdentityDomainException.class, () -> handler.update(command));
    }

    @Test
    void shouldThrowExceptionWhenRequesterDetailsMissing() {
        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                Collections.emptyList(),
                requesterId,
                null,
                null
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetMerchantUser));

        assertThrows(IdentityDomainException.class, () -> handler.update(command));
    }

    @Test
    void shouldThrowExceptionWhenRequesterIsNotMerchant() {
        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                Collections.emptyList(),
                requesterId,
                UserType.INTERNAL,
                List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetMerchantUser));

        assertThrows(IdentityDomainException.class, () -> handler.update(command));
    }

    @Test
    void shouldThrowExceptionWhenRequesterLacksPermission() {
        Role unauthorizedRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .organizationalUnitId(merchantOrgUnitId)
                .permissions(Collections.emptyList())
                .build();

        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                Collections.emptyList(),
                requesterId,
                UserType.MERCHANT,
                List.of(unauthorizedRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetMerchantUser));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(unauthorizedRole));

        assertThrows(IdentityDomainException.class, () -> handler.update(command));
    }

    @Test
    void shouldThrowExceptionWhenTargetUserIsNotMerchant() {
        User customerUser = User.builder()
                .userId(new UserId(userId))
                .userType(UserType.CUSTOMER)
                .build();

        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                Collections.emptyList(),
                requesterId,
                UserType.MERCHANT,
                List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(customerUser));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterRestaurantRole));

        assertThrows(IdentityDomainException.class, () -> handler.update(command));
    }

    @Test
    void shouldThrowExceptionWhenTargetUserDoesNotBelongToOrg() {
        User otherOrgUser = User.builder()
                .userId(new UserId(userId))
                .userType(UserType.MERCHANT)
                .organizationalUnitIds(List.of(UUID.randomUUID())) // Different org
                .build();

        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                Collections.emptyList(),
                requesterId,
                UserType.MERCHANT,
                List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(otherOrgUser));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterRestaurantRole));

        assertThrows(IdentityDomainException.class, () -> handler.update(command));
    }

    @Test
    void shouldThrowExceptionWhenAnyTargetRoleNotFound() {
        UUID nonExistentRoleId = UUID.randomUUID();

        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                List.of(nonExistentRoleId),
                requesterId,
                UserType.MERCHANT,
                List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetMerchantUser));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterRestaurantRole));
        when(roleRepository.findAllById(List.of(nonExistentRoleId))).thenReturn(Collections.emptyList());

        assertThrows(IdentityDomainException.class, () -> handler.update(command));
    }

    @Test
    void shouldThrowExceptionWhenTargetRoleIsStatic() {
        Role staticRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .name("MERCHANT_BASE")
                .organizationalUnitId(merchantOrgUnitId)
                .isStatic(true)
                .build();

        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                List.of(staticRole.getId().getValue()),
                requesterId,
                UserType.MERCHANT,
                List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetMerchantUser));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterRestaurantRole));
        when(roleRepository.findAllById(List.of(staticRole.getId().getValue()))).thenReturn(List.of(staticRole));

        assertThrows(IdentityDomainException.class, () -> handler.update(command));
    }

    @Test
    void shouldThrowExceptionWhenTargetRoleBelongsToDifferentOrg() {
        UUID otherOrg = UUID.randomUUID();
        Role otherOrgRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .name("OTHER_ROLE")
                .organizationalUnitId(otherOrg)
                .isStatic(false)
                .build();

        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                List.of(otherOrgRole.getId().getValue()),
                requesterId,
                UserType.MERCHANT,
                List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetMerchantUser));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterRestaurantRole));
        when(roleRepository.findAllById(List.of(otherOrgRole.getId().getValue()))).thenReturn(List.of(otherOrgRole));

        assertThrows(IdentityDomainException.class, () -> handler.update(command));
    }

    @Test
    void shouldLeaveIntentStartedWhenKeycloakFails() {
        UUID role1Id = restaurantRole1.getId().getValue();

        UpdateMerchantUserRolesCommand command = new UpdateMerchantUserRolesCommand(
                userId,
                merchantOrgUnitId,
                List.of(role1Id),
                requesterId,
                UserType.MERCHANT,
                List.of(requesterRestaurantRole.getId().getValue())
        );

        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(targetMerchantUser));
        when(roleRepository.findAllById(command.requesterRoleIds())).thenReturn(List.of(requesterRestaurantRole));
        when(roleRepository.findAllById(List.of(role1Id))).thenReturn(List.of(restaurantRole1));

        UserUpdateIntent mockIntent = mock(UserUpdateIntent.class);
        when(userUpdateIntentHelper.createIntent(eq(userId), eq("UPDATE_USER"), anyString(), anyString()))
                .thenReturn(mockIntent);

        doThrow(new RuntimeException("Keycloak connection error"))
                .when(identityProviderPort).updateUserRolesAndBranches(any(), any(), any());

        assertThrows(IdentityDomainException.class, () -> handler.update(command));

        verify(userUpdateIntentHelper, never()).markKeycloakDone(any());
        verify(userUpdateIntentHelper, never()).completeIntent(any(), any());
        verify(tokenRevocationPort, never()).revokeAccessToken(any());
    }
}
