package com.berkay.identity.service.handler.helper;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.IntentId;
import com.berkay.identity.service.domain.valueobject.IntentStatus;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.ports.output.repository.UserUpdateIntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserUpdateIntentHelper {

    private final UserUpdateIntentRepository intentRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserUpdateIntent createIntent(UUID currentUserId, String commandType, String newSnapshot, String oldSnapshot) {
        try {
            log.info("Creating update intent for user: {}, command: {}", currentUserId, commandType);
            
            // Check if user exists before creating intent
            userRepository.findById(new UserId(currentUserId))
                    .orElseThrow(() -> new IdentityDomainException("User not found: " + currentUserId));

            UserUpdateIntent intent = UserUpdateIntent.Builder.builder()
                    .intentId(new IntentId(UUID.randomUUID()))
                    .userId(new UserId(currentUserId))
                    .status(IntentStatus.STARTED)
                    .commandType(commandType)
                    .oldSnapshot(oldSnapshot)
                    .newSnapshot(newSnapshot)
                    .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                    .build();
                    
            return intentRepository.save(intent);
        } catch (DataIntegrityViolationException e) {
            // This catches the unique constraint violation on idx_user_active_intent
            log.warn("Concurrent update detected for user: {}. An active intent already exists.", currentUserId);
            throw new IdentityDomainException("Another update operation is currently in progress for this user. Please try again later.");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeIntent(UUID intentId, Consumer<User> updateAction) {
        log.info("Completing update intent: {}", intentId);
        
        UserUpdateIntent intent = intentRepository.findById(intentId.toString())
                .orElseThrow(() -> new IdentityDomainException("Intent not found: " + intentId));

        if (intent.getStatus() == IntentStatus.COMPLETED) {
            log.warn("Intent {} is already completed.", intentId);
            return;
        }

        User user = userRepository.findById(intent.getUserId())
                .orElseThrow(() -> new IdentityDomainException("User not found: " + intent.getUserId().getValue()));

        // Perform the DB update
        updateAction.accept(user);
        userRepository.save(user);

        // Complete the intent
        intent.markAsKeycloakDone(); // Keycloak done status is intermediate, but we can set it and then complete
        intent.complete();
        intentRepository.save(intent);
        
        log.info("Successfully completed update intent: {}", intentId);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markKeycloakDone(UUID intentId) {
        UserUpdateIntent intent = intentRepository.findById(intentId.toString())
                .orElseThrow(() -> new IdentityDomainException("Intent not found: " + intentId));
                
        intent.markAsKeycloakDone();
        intentRepository.save(intent);
    }
}
