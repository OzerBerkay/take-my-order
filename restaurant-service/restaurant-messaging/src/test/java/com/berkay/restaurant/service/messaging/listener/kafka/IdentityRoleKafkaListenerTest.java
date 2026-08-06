package com.berkay.restaurant.service.messaging.listener.kafka;

import com.berkay.kafka.identity.avro.model.RoleEventAvroModel;
import com.berkay.kafka.identity.avro.model.PermissionAvroModel;
import com.berkay.restaurant.service.domain.dto.message.RoleEventPayload;
import com.berkay.restaurant.service.domain.ports.input.message.listener.role.RoleMessageListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityRoleKafkaListenerTest {

    @Mock
    private RoleMessageListener roleMessageListener;

    @InjectMocks
    private IdentityRoleKafkaListener identityRoleKafkaListener;

    @Test
    void shouldProcessRoleCreatedEvent() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        RoleEventAvroModel avroModel = RoleEventAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setRoleId(roleId)
                .setName("TEST_ROLE")
                .setUserType("MERCHANT")
                .setEventType("ROLE_CREATED")
                .setVersion(0L)
                .setPermissions(List.of(
                        PermissionAvroModel.newBuilder()
                                .setId(UUID.randomUUID())
                                .setCode("TEST_PERM")
                                .setDomain("TEST_DOMAIN")
                                .setActive(true)
                                .setIsRestricted(false)
                                .build()
                ))
                .build();

        // Act
        identityRoleKafkaListener.receive(List.of(avroModel), List.of("key"), List.of(0), List.of(0L));

        // Assert
        ArgumentCaptor<RoleEventPayload> payloadCaptor = ArgumentCaptor.forClass(RoleEventPayload.class);
        verify(roleMessageListener, times(1)).roleCreated(payloadCaptor.capture());
        
        RoleEventPayload payload = payloadCaptor.getValue();
        assertEquals(roleId, payload.getRoleId());
        assertEquals("TEST_ROLE", payload.getName());
        assertEquals("ROLE_CREATED", payload.getEventType());
        assertNotNull(payload.getPermissions());
        assertEquals(1, payload.getPermissions().size());
        assertEquals("TEST_PERM", payload.getPermissions().get(0).getCode());
    }
}
