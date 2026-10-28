package com.berkay.identity.service.scheduler;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.valueobject.IntentId;
import com.berkay.identity.service.domain.valueobject.IntentStatus;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.UpdateUserProfileCommand;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.ports.output.repository.UserUpdateIntentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUpdateIntentSchedulerTest {

    @Mock
    private UserUpdateIntentRepository userUpdateIntentRepository;

    @Mock
    private IdentityProviderPort identityProviderPort;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserUpdateIntentHelper intentHelper;

    @InjectMocks
    private UserUpdateIntentScheduler scheduler;

    private UUID userId;
    private UUID intentId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        intentId = UUID.randomUUID();
    }

    @Test
    void shouldDiscardStaleIntentOlderThan15Minutes() {
        UserUpdateIntent staleIntent = UserUpdateIntent.Builder.builder()
                .intentId(new IntentId(intentId))
                .userId(new UserId(userId))
                .status(IntentStatus.STARTED)
                .commandType("REMOVE_RESTAURANT_PERSONNEL")
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(20))
                .build();

        when(userUpdateIntentRepository.findByStatusIn(any())).thenReturn(List.of(staleIntent));

        scheduler.processPendingIntents();

        assertEquals(IntentStatus.DISCARDED, staleIntent.getStatus());
        verify(userUpdateIntentRepository).save(staleIntent);
        verifyNoInteractions(identityProviderPort);
    }

    @Test
    void shouldDiscardUnknownCommandType() {
        UserUpdateIntent unknownIntent = UserUpdateIntent.Builder.builder()
                .intentId(new IntentId(intentId))
                .userId(new UserId(userId))
                .status(IntentStatus.STARTED)
                .commandType("UNKNOWN_CUSTOM_COMMAND")
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(2))
                .build();

        User user = mock(User.class);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userUpdateIntentRepository.findByStatusIn(any())).thenReturn(List.of(unknownIntent));

        scheduler.processPendingIntents();

        assertEquals(IntentStatus.DISCARDED, unknownIntent.getStatus());
        verify(userUpdateIntentRepository).save(unknownIntent);
        verifyNoInteractions(identityProviderPort);
    }

    @Test
    void shouldRecoverUserProfileCommand() throws Exception {
        UserUpdateIntent profileIntent = UserUpdateIntent.Builder.builder()
                .intentId(new IntentId(intentId))
                .userId(new UserId(userId))
                .status(IntentStatus.STARTED)
                .commandType(UpdateUserProfileCommand.class.getSimpleName())
                .newSnapshot("{\"firstName\":\"John\",\"lastName\":\"Doe\"}")
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(2))
                .build();

        User user = mock(User.class);
        when(user.getExternalId()).thenReturn("ext-123");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userUpdateIntentRepository.findByStatusIn(any())).thenReturn(List.of(profileIntent));

        UpdateUserProfileCommand command = UpdateUserProfileCommand.builder()
                .firstName("John")
                .lastName("Doe")
                .build();
        when(objectMapper.readValue(anyString(), eq(UpdateUserProfileCommand.class))).thenReturn(command);

        scheduler.processPendingIntents();

        verify(identityProviderPort).updateUserProfile("ext-123", "John", "Doe");
        verify(intentHelper).markKeycloakDone(intentId);
        verify(intentHelper).completeIntent(eq(intentId), any());
    }
}
