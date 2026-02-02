package com.berkay.identity.service.outbox.scheduler;

import com.berkay.identity.service.outbox.model.DomainEventType;
import com.berkay.identity.service.outbox.model.UserEventPayload;
import com.berkay.identity.service.outbox.model.UserOutboxMessage;
import com.berkay.identity.service.ports.output.repository.UserOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class UserOutboxHelper {


    private final UserOutboxRepository userOutboxRepository;
    private final ObjectMapper objectMapper;

    public UserOutboxHelper(UserOutboxRepository userOutboxRepository, ObjectMapper objectMapper) {
        this.userOutboxRepository = userOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveUserOutboxMessage(UserEventPayload userEventPayload, DomainEventType eventType) {
        save(UserOutboxMessage.builder()
                .id(UUID.randomUUID())
                .createdAt(ZonedDateTime.now())
                .type(eventType.name())
                .payload(createPayload(userEventPayload))
                .outboxStatus(OutboxStatus.STARTED)
                .build());
    }

    @Transactional(readOnly = true)
    public List<UserOutboxMessage> getUserOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        return userOutboxRepository.findByOutboxStatus(outboxStatus);
    }

    @Transactional
    public void deleteUserOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        userOutboxRepository.deleteByOutboxStatus(outboxStatus);
    }

    // Callback fonksiyonu olarak kullanılacak
    @Transactional
    public void updateOutboxMessage(UserOutboxMessage userOutboxMessage, OutboxStatus outboxStatus) {
        userOutboxMessage.setOutboxStatus(outboxStatus);
        userOutboxMessage.setProcessedAt(ZonedDateTime.now());
        save(userOutboxMessage);
        log.info("User outbox table status is updated as: {}", outboxStatus.name());
    }

    private String createPayload(UserEventPayload userEventPayload) {
        try {
            return objectMapper.writeValueAsString(userEventPayload);
        } catch (JsonProcessingException e) {
            log.error("Could not create UserEventPayload json!", e);
            throw new RuntimeException("Could not create UserEventPayload json!", e);
        }
    }

    private void save(UserOutboxMessage userOutboxMessage) {
        UserOutboxMessage response = userOutboxRepository.save(userOutboxMessage);
        if (response == null) {
            log.error("Could not save UserOutboxMessage!");
            throw new RuntimeException("Could not save UserOutboxMessage!");
        }
        log.info("UserOutboxMessage is saved with id: {}", userOutboxMessage.getId());
    }
}