package com.berkay.identity.service.application.listener;

import com.berkay.identity.service.domain.entity.OrganizationalUnit;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.IntentId;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitId;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.OrganizationalUnitRepository;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.kafka.order.avro.model.RestaurantPersonnelAvroModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantPersonnelMessageListenerImplTest {

    @Mock
    private UserUpdateIntentHelper userUpdateIntentHelper;

    @Mock
    private OrganizationalUnitRepository organizationalUnitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdentityProviderPort identityProviderPort;

    @Mock
    private TokenRevocationPort tokenRevocationPort;

    @InjectMocks
    private RestaurantPersonnelMessageListenerImpl messageListener;

    private UUID userId;
    private UUID restaurantId;
    private RestaurantPersonnelAvroModel payload;
    private OrganizationalUnit orgUnit;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();

        payload = mock(RestaurantPersonnelAvroModel.class);
        when(payload.getUserId()).thenReturn(userId);
        when(payload.getRestaurantId()).thenReturn(restaurantId);

        orgUnit = OrganizationalUnit.builder()
                .id(new OrganizationalUnitId(restaurantId))
                .name("Test Restaurant")
                .build();

        user = mock(User.class);
        lenient().when(user.getExternalId()).thenReturn("keycloak-" + userId);
    }

    @Test
    void shouldSuccessfullyProcessPersonnelRemoved() {
        // Arrange
        when(organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))).thenReturn(Optional.of(orgUnit));
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(user));

        Role roleToKeep = mock(Role.class);
        UUID roleToKeepId = UUID.randomUUID();
        when(roleToKeep.getId()).thenReturn(new RoleId(roleToKeepId));
        when(roleToKeep.getOrganizationalUnitId()).thenReturn(UUID.randomUUID());

        Role roleToRemove = mock(Role.class);
        UUID roleToRemoveId = UUID.randomUUID();
        when(roleToRemove.getOrganizationalUnitId()).thenReturn(restaurantId);

        when(user.getRoles()).thenReturn(List.of(roleToKeep, roleToRemove));
        when(user.getOrganizationalUnitIds()).thenReturn(List.of(restaurantId, UUID.randomUUID()));

        UserUpdateIntent intent = UserUpdateIntent.builder()
                .intentId(new IntentId(UUID.randomUUID()))
                .build();

        when(userUpdateIntentHelper.createIntent(eq(userId), eq("REMOVE_RESTAURANT_PERSONNEL"), eq("{}"), eq("{}"))).thenReturn(intent);

        doAnswer(invocation -> {
            Consumer<User> consumer = invocation.getArgument(1);
            consumer.accept(user);
            return null;
        }).when(userUpdateIntentHelper).completeIntent(eq(intent.getId().getValue()), any());

        // Act
        messageListener.personnelRemoved(payload);

        // Assert
        verify(organizationalUnitRepository, times(1)).findById(new OrganizationalUnitId(restaurantId));
        verify(userRepository, times(1)).findById(new UserId(userId));
        verify(identityProviderPort, times(1)).updateUserRolesAndBranches(
                eq("keycloak-" + userId),
                argThat(roles -> roles.contains(roleToKeepId.toString()) && !roles.contains(roleToRemoveId.toString())),
                argThat(orgUnits -> !orgUnits.contains(restaurantId.toString()))
        );
        verify(userUpdateIntentHelper, times(1)).markKeycloakDone(intent.getId().getValue());
        verify(userUpdateIntentHelper, times(1)).completeIntent(eq(intent.getId().getValue()), any());
        verify(user, times(1)).removeOrganizationalUnit(orgUnit);
        verify(user, times(1)).removeRole(roleToRemove);
        verify(user, never()).removeRole(roleToKeep);
        verify(tokenRevocationPort, times(1)).revokeAccessToken(userId);
    }

    @Test
    void shouldThrowExceptionWhenOrgUnitNotFoundOnPersonnelRemoved() {
        // Arrange
        when(organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IdentityDomainException.class, () -> {
            messageListener.personnelRemoved(payload);
        });

        verify(organizationalUnitRepository, times(1)).findById(new OrganizationalUnitId(restaurantId));
        verify(userRepository, never()).findById(any());
        verify(userUpdateIntentHelper, never()).createIntent(any(), any(), any(), any());
        verify(identityProviderPort, never()).updateUserRolesAndBranches(any(), any(), any());
        verify(tokenRevocationPort, never()).revokeAccessToken(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundOnPersonnelRemoved() {
        // Arrange
        when(organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))).thenReturn(Optional.of(orgUnit));
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IdentityDomainException.class, () -> {
            messageListener.personnelRemoved(payload);
        });

        verify(userRepository, times(1)).findById(new UserId(userId));
        verify(userUpdateIntentHelper, never()).createIntent(any(), any(), any(), any());
        verify(identityProviderPort, never()).updateUserRolesAndBranches(any(), any(), any());
        verify(tokenRevocationPort, never()).revokeAccessToken(any());
    }

    @Test
    void shouldThrowExceptionWhenKeycloakFailsOnPersonnelRemoved() {
        // Arrange
        when(organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))).thenReturn(Optional.of(orgUnit));
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(user));
        when(user.getRoles()).thenReturn(List.of());
        when(user.getOrganizationalUnitIds()).thenReturn(List.of(restaurantId));

        UserUpdateIntent intent = UserUpdateIntent.builder()
                .intentId(new IntentId(UUID.randomUUID()))
                .build();
        when(userUpdateIntentHelper.createIntent(eq(userId), eq("REMOVE_RESTAURANT_PERSONNEL"), eq("{}"), eq("{}"))).thenReturn(intent);

        doThrow(new RuntimeException("Keycloak connection error"))
                .when(identityProviderPort).updateUserRolesAndBranches(any(), any(), any());

        // Act & Assert
        assertThrows(IdentityDomainException.class, () -> {
            messageListener.personnelRemoved(payload);
        });

        verify(identityProviderPort, times(1)).updateUserRolesAndBranches(any(), any(), any());
        verify(userUpdateIntentHelper, never()).markKeycloakDone(any());
        verify(userUpdateIntentHelper, never()).completeIntent(any(), any());
        verify(tokenRevocationPort, never()).revokeAccessToken(any());
    }

    @Test
    void shouldSuccessfullyProcessPersonnelAdded() {
        // Arrange
        when(organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))).thenReturn(Optional.of(orgUnit));
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(user));

        UUID existingOrgUnit = UUID.randomUUID();
        when(user.getOrganizationalUnitIds()).thenReturn(List.of(existingOrgUnit));

        Role existingRole = mock(Role.class);
        UUID roleId = UUID.randomUUID();
        when(existingRole.getId()).thenReturn(new RoleId(roleId));
        when(user.getRoles()).thenReturn(List.of(existingRole));

        UserUpdateIntent intent = UserUpdateIntent.builder()
                .intentId(new IntentId(UUID.randomUUID()))
                .build();
        when(userUpdateIntentHelper.createIntent(eq(userId), eq("ASSIGN_RESTAURANT_PERSONNEL"), eq("{}"), eq("{}"))).thenReturn(intent);

        doAnswer(invocation -> {
            Consumer<User> consumer = invocation.getArgument(1);
            consumer.accept(user);
            return null;
        }).when(userUpdateIntentHelper).completeIntent(eq(intent.getId().getValue()), any());

        // Act
        messageListener.personnelAdded(payload);

        // Assert
        verify(organizationalUnitRepository, times(1)).findById(new OrganizationalUnitId(restaurantId));
        verify(userRepository, times(1)).findById(new UserId(userId));
        verify(identityProviderPort, times(1)).updateUserRolesAndBranches(
                eq("keycloak-" + userId),
                argThat(roles -> roles.contains(roleId.toString())),
                argThat(orgUnits -> orgUnits.contains(existingOrgUnit.toString()) && orgUnits.contains(restaurantId.toString()))
        );
        verify(userUpdateIntentHelper, times(1)).markKeycloakDone(intent.getId().getValue());
        verify(userUpdateIntentHelper, times(1)).completeIntent(eq(intent.getId().getValue()), any());
        verify(user, times(1)).addOrganizationalUnit(orgUnit);
        verify(tokenRevocationPort, times(1)).revokeAccessToken(userId);
    }

    @Test
    void shouldThrowExceptionWhenOrgUnitNotFoundOnPersonnelAdded() {
        // Arrange
        when(organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IdentityDomainException.class, () -> {
            messageListener.personnelAdded(payload);
        });

        verify(userRepository, never()).findById(any());
        verify(identityProviderPort, never()).updateUserRolesAndBranches(any(), any(), any());
        verify(tokenRevocationPort, never()).revokeAccessToken(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundOnPersonnelAdded() {
        // Arrange
        when(organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))).thenReturn(Optional.of(orgUnit));
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IdentityDomainException.class, () -> {
            messageListener.personnelAdded(payload);
        });

        verify(identityProviderPort, never()).updateUserRolesAndBranches(any(), any(), any());
        verify(tokenRevocationPort, never()).revokeAccessToken(any());
    }

    @Test
    void shouldThrowExceptionWhenKeycloakFailsOnPersonnelAdded() {
        // Arrange
        when(organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))).thenReturn(Optional.of(orgUnit));
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(user));
        when(user.getRoles()).thenReturn(List.of());
        when(user.getOrganizationalUnitIds()).thenReturn(List.of());

        UserUpdateIntent intent = UserUpdateIntent.builder()
                .intentId(new IntentId(UUID.randomUUID()))
                .build();
        when(userUpdateIntentHelper.createIntent(eq(userId), eq("ASSIGN_RESTAURANT_PERSONNEL"), eq("{}"), eq("{}"))).thenReturn(intent);

        doThrow(new RuntimeException("Keycloak connection error"))
                .when(identityProviderPort).updateUserRolesAndBranches(any(), any(), any());

        // Act & Assert
        assertThrows(IdentityDomainException.class, () -> {
            messageListener.personnelAdded(payload);
        });

        verify(identityProviderPort, times(1)).updateUserRolesAndBranches(any(), any(), any());
        verify(userUpdateIntentHelper, never()).markKeycloakDone(any());
        verify(userUpdateIntentHelper, never()).completeIntent(any(), any());
        verify(tokenRevocationPort, never()).revokeAccessToken(any());
    }
}
