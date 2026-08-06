package com.berkay.identity.service.scheduler;

import com.berkay.identity.service.domain.valueobject.AccountStatus;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.valueobject.FirstName;
import com.berkay.identity.service.domain.valueobject.IntentStatus;
import com.berkay.identity.service.domain.valueobject.LastName;
import com.berkay.identity.service.dto.command.UpdateUserProfileCommand;
import com.berkay.identity.service.dto.command.UpdateUserStatusCommand;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.ports.output.repository.UserUpdateIntentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserUpdateIntentScheduler {

    private final UserUpdateIntentRepository userUpdateIntentRepository;
    private final IdentityProviderPort identityProviderPort;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final UserUpdateIntentHelper intentHelper;

    @Scheduled(fixedDelay = 60000) // Runs every minute
    // NO @Transactional Here! This allows the scheduler to make Keycloak calls without holding a DB connection open
    public void processPendingIntents() {
        List<UserUpdateIntent> intents = userUpdateIntentRepository.findByStatusIn(
                List.of(IntentStatus.STARTED, IntentStatus.KEYCLOAK_DONE));

        if (!intents.isEmpty()) {
            log.info("Found {} pending intents for recovery.", intents.size());
        }

        for (UserUpdateIntent intent : intents) {
            try {
                if (intent.getStatus() == IntentStatus.STARTED) {
                    processStartedIntent(intent);
                } else if (intent.getStatus() == IntentStatus.KEYCLOAK_DONE) {
                    processKeycloakDoneIntent(intent);
                }
            } catch (Exception e) {
                log.error("Failed to recover intent id: {}", intent.getId().getValue(), e);
            }
        }
    }

    private void processStartedIntent(UserUpdateIntent intent) throws Exception {
        log.info("Recovering STARTED intent id: {}", intent.getId().getValue());
        User user = userRepository.findById(intent.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + intent.getUserId().getValue()));

        String commandType = intent.getCommandType();
        
        // Retry Keycloak update
        if (UpdateUserProfileCommand.class.getSimpleName().equals(commandType)) {
            UpdateUserProfileCommand command = objectMapper.readValue(intent.getNewSnapshot(), UpdateUserProfileCommand.class);
            identityProviderPort.updateUserProfile(user.getExternalId(), command.getFirstName(), command.getLastName());
        } else if (UpdateUserStatusCommand.class.getSimpleName().equals(commandType)) {
            UpdateUserStatusCommand command = objectMapper.readValue(intent.getNewSnapshot(), UpdateUserStatusCommand.class);
            boolean isEnabled = command.getStatus() == AccountStatus.ACTIVE;
            identityProviderPort.updateUserStatus(user.getExternalId(), isEnabled);

        } else {
            log.warn("Unknown commandType for intent recovery: {}", commandType);
            return;
        }

        // Mark as Keycloak Done using the new isolated transaction
        intentHelper.markKeycloakDone(intent.getId().getValue());

        // Proceed to update DB immediately
        processKeycloakDoneIntent(intent);
    }

    private void processKeycloakDoneIntent(UserUpdateIntent intent) throws Exception {
        log.info("Recovering KEYCLOAK_DONE intent id: {}", intent.getId().getValue());
        
        String commandType = intent.getCommandType();
        
        intentHelper.completeIntent(intent.getId().getValue(), u -> {
            try {
                if (UpdateUserProfileCommand.class.getSimpleName().equals(commandType)) {
                    UpdateUserProfileCommand command = objectMapper.readValue(intent.getNewSnapshot(), UpdateUserProfileCommand.class);
                    u.updateProfile(new FirstName(command.getFirstName()), new LastName(command.getLastName()), command.getImageUrl());
                } else if (UpdateUserStatusCommand.class.getSimpleName().equals(commandType)) {
                    UpdateUserStatusCommand command = objectMapper.readValue(intent.getNewSnapshot(), UpdateUserStatusCommand.class);
                    u.updateStatus(command.getStatus());

                }
            } catch (Exception e) {
                throw new RuntimeException("Error applying update during intent recovery", e);
            }
        });
    }
}
