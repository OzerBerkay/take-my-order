package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.IntentId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.UpdateUserProfileCommand;
import com.berkay.identity.service.dto.command.UpdateUserProfileResponse;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.ports.output.security.SecurityContextPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdentityProviderPort identityProviderPort;

    @Mock
    private TokenRevocationPort tokenRevocationPort;

    @Mock
    private SecurityContextPort securityContextPort;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserUpdateIntentHelper intentHelper;

    @InjectMocks
    private UpdateUserProfileCommandHandler handler;

    private UUID userId;
    private UpdateUserProfileCommand command;
    private User user;
    private UserUpdateIntent intent;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        command = UpdateUserProfileCommand.builder()
                .firstName("NewName")
                .lastName("NewLastName")
                .imageUrl("http://example.com/img.jpg")
                .build();

        user = User.Builder.builder()
                .externalId("ext-123")
                .build();
        user.setId(new UserId(userId));

        intent = UserUpdateIntent.Builder.builder()
                .intentId(new IntentId(UUID.randomUUID()))
                .build();
    }

    @Test
    @DisplayName("Başarılı Senaryo: Profil başarılı bir şekilde güncellenir")
    void shouldUpdateProfileSuccessfully() {
        // Given
        when(securityContextPort.getCurrentInternalUserId()).thenReturn(userId);
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(user));
        when(intentHelper.createIntent(eq(userId), any(), any(), any())).thenReturn(intent);
        
        doNothing().when(identityProviderPort).updateUserProfile("ext-123", "NewName", "NewLastName");
        doNothing().when(intentHelper).markKeycloakDone(intent.getId().getValue());
        doAnswer(invocation -> {
            Consumer<User> action = invocation.getArgument(1);
            action.accept(user);
            return null;
        }).when(intentHelper).completeIntent(eq(intent.getId().getValue()), any());

        // When
        UpdateUserProfileResponse response = handler.updateUserProfile(command);

        // Then
        assertNotNull(response);
        assertEquals("Profile successfully updated", response.getMessage());
        verify(identityProviderPort, times(1)).updateUserProfile("ext-123", "NewName", "NewLastName");
        verify(intentHelper, times(1)).markKeycloakDone(intent.getId().getValue());
        verify(intentHelper, times(1)).completeIntent(eq(intent.getId().getValue()), any());
        verify(tokenRevocationPort, times(1)).revokeAccessToken(userId);
    }

    @Test
    @DisplayName("Alternatif Senaryo: Kullanıcı bulunamazsa Exception fırlatır")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(securityContextPort.getCurrentInternalUserId()).thenReturn(userId);
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.empty());

        // When / Then
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> {
            handler.updateUserProfile(command);
        });

        assertEquals("User not found: " + userId, exception.getMessage());
        verify(intentHelper, never()).createIntent(any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Alternatif Senaryo: Keycloak güncellenemezse Intent STARTED olarak kalır ve Exception fırlatır")
    void shouldThrowExceptionWhenKeycloakUpdateFails() {
        // Given
        when(securityContextPort.getCurrentInternalUserId()).thenReturn(userId);
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(user));
        when(intentHelper.createIntent(eq(userId), any(), any(), any())).thenReturn(intent);

        doThrow(new RuntimeException("Keycloak connection error"))
                .when(identityProviderPort).updateUserProfile(anyString(), anyString(), anyString());

        // When / Then
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> {
            handler.updateUserProfile(command);
        });

        assertEquals("Failed to update user profile in Keycloak. System will retry automatically.", exception.getMessage());
        verify(intentHelper, never()).markKeycloakDone(any());
        verify(intentHelper, never()).completeIntent(any(), any());
    }
}
