package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.FirstName;
import com.berkay.identity.service.domain.valueobject.LastName;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.UpdateUserProfileCommand;
import com.berkay.identity.service.dto.command.UpdateUserProfileResponse;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.ports.output.security.SecurityContextPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateUserProfileCommandHandler {

    private final UserRepository userRepository;
    private final IdentityProviderPort identityProviderPort;
    private final TokenRevocationPort tokenRevocationPort;
    private final SecurityContextPort securityContextPort;
    private final ObjectMapper objectMapper;
    private final UserUpdateIntentHelper intentHelper;

    public UpdateUserProfileResponse updateUserProfile(UpdateUserProfileCommand command) {
        UUID currentUserId = securityContextPort.getCurrentInternalUserId();
        log.info("Updating user profile for user id: {}", currentUserId);

        User user = userRepository.findById(new UserId(currentUserId))
                .orElseThrow(() -> new IdentityDomainException("User not found: " + currentUserId));

        // 1. Transaction 1: Create Intent (Locks the user for updates)
        UserUpdateIntent intent = intentHelper.createIntent(
                currentUserId,
                UpdateUserProfileCommand.class.getSimpleName(),
                serializeCommand(command),
                serializeCurrentState(user)
        );

        // 2. No Transaction: Call Keycloak
        try {
            identityProviderPort.updateUserProfile(user.getExternalId(), command.getFirstName(), command.getLastName());
            // Optional: Mark as keycloak done to optimize recovery in case DB update fails
            intentHelper.markKeycloakDone(intent.getId().getValue());
        } catch (Exception e) {
            log.error("Failed to update user in Keycloak, intent remains STARTED for recovery. UserId: {}", currentUserId, e);
            throw new IdentityDomainException("Failed to update user profile in Keycloak. System will retry automatically.");
        }

        // 3. Transaction 2: Update DB and Complete Intent
        intentHelper.completeIntent(intent.getId().getValue(), u -> {
            u.updateProfile(new FirstName(command.getFirstName()), new LastName(command.getLastName()), command.getImageUrl());
        });

        tokenRevocationPort.revokeAccessToken(currentUserId);

        log.info("User profile successfully updated for user id: {}", currentUserId);
        return UpdateUserProfileResponse.builder()
                .message("Profile successfully updated")
                .build();
    }

    private String serializeCurrentState(User user) {
        try {
            return objectMapper.writeValueAsString(UpdateUserProfileCommand.builder()
                    .firstName(user.getFirstName() != null ? user.getFirstName().getValue() : null)
                    .lastName(user.getLastName() != null ? user.getLastName().getValue() : null)
                    .imageUrl(user.getImageUrl())
                    .build());
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String serializeCommand(UpdateUserProfileCommand command) {
        try {
            return objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
