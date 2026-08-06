package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.message.RoleEventPayload;
import com.berkay.restaurant.service.domain.ports.output.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleMessageListenerImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleMessageListenerImpl roleMessageListener;

    @Test
    void shouldSaveOnRoleCreated() {
        RoleEventPayload payload = RoleEventPayload.builder()
                .roleId(UUID.randomUUID())
                .name("TEST")
                .build();

        roleMessageListener.roleCreated(payload);
        verify(roleRepository, times(1)).save(payload);
    }

    @Test
    void shouldSaveOnRoleUpdated() {
        RoleEventPayload payload = RoleEventPayload.builder()
                .roleId(UUID.randomUUID())
                .name("TEST_UPDATE")
                .build();

        roleMessageListener.roleUpdated(payload);
        verify(roleRepository, times(1)).save(payload);
    }

    @Test
    void shouldDeleteOnRoleDeleted() {
        UUID id = UUID.randomUUID();
        RoleEventPayload payload = RoleEventPayload.builder()
                .roleId(id)
                .build();

        roleMessageListener.roleDeleted(payload);
        verify(roleRepository, times(1)).delete(id);
    }
}
