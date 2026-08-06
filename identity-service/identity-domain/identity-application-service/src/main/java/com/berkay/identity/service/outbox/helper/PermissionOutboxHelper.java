package com.berkay.identity.service.outbox.helper;

import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.outbox.model.permission.PermissionEventPayload;
import com.berkay.identity.service.outbox.model.permission.PermissionOutboxMessage;
import com.berkay.identity.service.ports.output.repository.PermissionOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionOutboxHelper {

    private final PermissionOutboxRepository permissionOutboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void savePermissionOutboxMessage(PermissionEventPayload payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            PermissionOutboxMessage outboxMessage = PermissionOutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                    .type(payload.getEventType())
                    .payload(payloadJson)
                    .outboxStatus(OutboxStatus.STARTED)
                    .version(null)
                    .build();

            permissionOutboxRepository.save(outboxMessage);
            log.info("PermissionOutboxMessage saved with id: {}", outboxMessage.getId());

        } catch (JsonProcessingException e) {
            log.error("Could not create PermissionEventPayload object for outbox!", e);
            throw new IdentityDomainException("Could not create PermissionEventPayload object for outbox!", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<List<PermissionOutboxMessage>> getPermissionOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        return permissionOutboxRepository.findByOutboxStatus(outboxStatus);
    }

    @Transactional
    public void deletePermissionOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        permissionOutboxRepository.deleteByOutboxStatus(outboxStatus);
        log.info("Deleted permission outbox messages with status: {}", outboxStatus.name());
    }

    @Transactional
    public void updateOutboxMessage(PermissionOutboxMessage permissionOutboxMessage, OutboxStatus outboxStatus) {
        permissionOutboxMessage.setOutboxStatus(outboxStatus);
        permissionOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        permissionOutboxRepository.save(permissionOutboxMessage);
        log.info("Permission outbox message status is updated as: {}", outboxStatus.name());
    }
}
