package com.berkay.identity.service.outbox.helper;

import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.outbox.model.role.RoleEventPayload;
import com.berkay.identity.service.outbox.model.role.RoleOutboxMessage;
import com.berkay.identity.service.ports.output.repository.RoleOutboxRepository;
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
public class RoleOutboxHelper {

    private final RoleOutboxRepository roleOutboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveRoleOutboxMessage(RoleEventPayload payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            RoleOutboxMessage outboxMessage = RoleOutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                    .type(payload.getEventType())
                    .payload(payloadJson)
                    .outboxStatus(OutboxStatus.STARTED)
                    .version(null)
                    .build();

            roleOutboxRepository.save(outboxMessage);
            log.info("RoleOutboxMessage saved with id: {}", outboxMessage.getId());

        } catch (JsonProcessingException e) {
            log.error("Could not create RoleEventPayload object for outbox!", e);
            throw new IdentityDomainException("Could not create RoleEventPayload object for outbox!", e);
        }
    }

    @Transactional(readOnly = true)
    public List<RoleOutboxMessage> getRoleOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        return roleOutboxRepository.findByOutboxStatus(outboxStatus);
    }

    @Transactional
    public void deleteRoleOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        roleOutboxRepository.deleteByOutboxStatus(outboxStatus);
        log.info("Deleted role outbox messages with status: {}", outboxStatus.name());
    }

    @Transactional
    public void updateOutboxMessage(RoleOutboxMessage roleOutboxMessage, OutboxStatus outboxStatus) {
        roleOutboxMessage.setOutboxStatus(outboxStatus);
        // İhtiyaç varsa RoleOutboxMessage içine processedAt alanı eklenebilir.
        roleOutboxRepository.save(roleOutboxMessage);
        log.info("Role outbox message status is updated as: {}", outboxStatus.name());
    }
}