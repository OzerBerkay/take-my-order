package com.berkay.identity.service.dataaccess.outbox.mapper;

import com.berkay.identity.service.dataaccess.outbox.entity.RoleOutboxEntity;
import com.berkay.identity.service.outbox.model.role.RoleOutboxMessage;
import com.berkay.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleOutboxDataAccessMapperTest {

    private RoleOutboxDataAccessMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RoleOutboxDataAccessMapper();
    }

    @Test
    void shouldMapEntityToMessageProperlyAndPreserveVersion() {
        // Arrange
        UUID id = UUID.randomUUID();
        ZonedDateTime now = ZonedDateTime.now();
        Long version = 0L; // Mocking JPA version column fetching

        RoleOutboxEntity entity = RoleOutboxEntity.builder()
                .id(id)
                .createdAt(now)
                .type("ROLE_CREATED")
                .payload("{\"test\":\"data\"}")
                .outboxStatus(OutboxStatus.STARTED)
                .version(version)
                .build();

        // Act
        RoleOutboxMessage message = mapper.roleOutboxEntityToOutboxMessage(entity);

        // Assert
        assertEquals(id, message.getId());
        assertEquals("ROLE_CREATED", message.getType());
        assertEquals(OutboxStatus.STARTED, message.getOutboxStatus());
        assertEquals(0L, message.getVersion());
    }

    @Test
    void shouldMapMessageToEntityProperlyAndPreserveVersion() {
        // Arrange
        UUID id = UUID.randomUUID();
        ZonedDateTime now = ZonedDateTime.now();

        RoleOutboxMessage message = RoleOutboxMessage.builder()
                .id(id)
                .createdAt(now)
                .type("ROLE_UPDATED")
                .payload("{\"test\":\"data\"}")
                .outboxStatus(OutboxStatus.COMPLETED)
                .version(1L)
                .build();

        // Act
        RoleOutboxEntity entity = mapper.roleOutboxMessageToOutboxEntity(message);

        // Assert
        assertEquals(id, entity.getId());
        assertEquals("ROLE_UPDATED", entity.getType());
        assertEquals(OutboxStatus.COMPLETED, entity.getOutboxStatus());
        assertEquals(1L, entity.getVersion());
    }
}
