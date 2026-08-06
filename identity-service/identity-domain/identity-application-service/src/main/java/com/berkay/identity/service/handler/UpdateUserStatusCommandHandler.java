package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.valueobject.AccountStatus;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.UpdateUserStatusCommand;
import com.berkay.identity.service.dto.command.UpdateUserStatusResponse;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateUserStatusCommandHandler {

    private final UserRepository userRepository;
    private final IdentityProviderPort identityProviderPort;
    private final TokenRevocationPort tokenRevocationPort;
    private final ObjectMapper objectMapper;
    private final UserUpdateIntentHelper intentHelper;

    public UpdateUserStatusResponse updateUserStatus(UpdateUserStatusCommand command) {
        UUID targetUserId = command.getTargetUserId();
        log.info("Updating user status for user id: {}", targetUserId);

        User user = userRepository.findById(new UserId(targetUserId))
                .orElseThrow(() -> new IdentityDomainException("User not found: " + targetUserId));

        // 1. Transaction 1: Create Intent (Locks the user for updates)
        UserUpdateIntent intent = intentHelper.createIntent(
                targetUserId,
                UpdateUserStatusCommand.class.getSimpleName(),
                serializeCommand(command),
                serializeCurrentState(user)
        );

        // 2. No Transaction: Call Keycloak
        try {
            boolean isEnabled = command.getStatus() == AccountStatus.ACTIVE;
            identityProviderPort.updateUserStatus(user.getExternalId(), isEnabled);
            intentHelper.markKeycloakDone(intent.getId().getValue());
        } catch (Exception e) {
            log.error("Failed to update user status in Keycloak, intent remains STARTED for recovery. UserId: {}", targetUserId, e);
            throw new IdentityDomainException("Failed to update user status in Keycloak. System will retry automatically.");
        }

        // 3. Transaction 2: Update DB and Complete Intent
        intentHelper.completeIntent(intent.getId().getValue(), u -> {
            u.updateStatus(command.getStatus());
        });

        if (command.getStatus() == AccountStatus.BANNED) {
            tokenRevocationPort.revokeAllTokens(targetUserId);
        }

        log.info("User status successfully updated for user id: {}", targetUserId);
        return UpdateUserStatusResponse.builder()
                .userId(targetUserId)
                .message("Status successfully updated")
                .build();
    }

    private String serializeCurrentState(User user) {
        try {
            return objectMapper.writeValueAsString(UpdateUserStatusCommand.builder()
                    .targetUserId(user.getId().getValue())
                    .status(user.getStatus())
                    .build());
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String serializeCommand(UpdateUserStatusCommand command) {
        try {
            return objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
