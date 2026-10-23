package com.berkay.order.service.dataaccess.outbox.delivery.adapter;

import com.berkay.order.service.dataaccess.outbox.delivery.entity.DeliveryOutboxEntity;
import com.berkay.order.service.dataaccess.outbox.delivery.mapper.DeliveryOutboxDataAccessMapper;
import com.berkay.order.service.dataaccess.outbox.delivery.repository.DeliveryOutboxJpaRepository;
import com.berkay.order.service.domain.outbox.model.delivery.DeliveryOutboxMessage;
import com.berkay.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeliveryOutboxRepositoryImplTest {

    @Mock
    private DeliveryOutboxJpaRepository deliveryOutboxJpaRepository;

    private DeliveryOutboxDataAccessMapper mapper;
    private DeliveryOutboxRepositoryImpl repository;

    private final UUID id = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mapper = new DeliveryOutboxDataAccessMapper();
        repository = new DeliveryOutboxRepositoryImpl(deliveryOutboxJpaRepository, mapper);
    }

    @Test
    void save_whenNewEntity_persistsNewEntity() {
        DeliveryOutboxMessage message = DeliveryOutboxMessage.builder()
                .id(id)
                .orderId(orderId)
                .createdAt(ZonedDateTime.now())
                .type("OrderDelivery")
                .payload("{}")
                .outboxStatus(OutboxStatus.STARTED)
                .version(0)
                .build();

        when(deliveryOutboxJpaRepository.findById(id)).thenReturn(Optional.empty());
        when(deliveryOutboxJpaRepository.save(any(DeliveryOutboxEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryOutboxMessage saved = repository.save(message);

        verify(deliveryOutboxJpaRepository).save(any(DeliveryOutboxEntity.class));
        assertEquals(id, saved.getId());
        assertEquals(OutboxStatus.STARTED, saved.getOutboxStatus());
    }

    @Test
    void save_whenExistingEntity_updatesInPlace() {
        DeliveryOutboxMessage message = DeliveryOutboxMessage.builder()
                .id(id)
                .orderId(orderId)
                .createdAt(ZonedDateTime.now())
                .processedAt(ZonedDateTime.now())
                .type("OrderDelivery")
                .payload("{\"updated\": true}")
                .outboxStatus(OutboxStatus.COMPLETED)
                .version(0)
                .build();

        DeliveryOutboxEntity existing = DeliveryOutboxEntity.builder()
                .id(id)
                .orderId(orderId)
                .createdAt(message.getCreatedAt())
                .type("OrderDelivery")
                .payload("{}")
                .outboxStatus(OutboxStatus.STARTED)
                .version(0)
                .build();

        when(deliveryOutboxJpaRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deliveryOutboxJpaRepository.save(any(DeliveryOutboxEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryOutboxMessage saved = repository.save(message);

        assertEquals(OutboxStatus.COMPLETED, saved.getOutboxStatus());
        assertEquals(OutboxStatus.COMPLETED, existing.getOutboxStatus());
    }

    @Test
    void findByTypeAndOutboxStatus_returnsList() {
        DeliveryOutboxEntity entity = DeliveryOutboxEntity.builder()
                .id(id)
                .orderId(orderId)
                .createdAt(ZonedDateTime.now())
                .type("OrderDelivery")
                .payload("{}")
                .outboxStatus(OutboxStatus.STARTED)
                .version(0)
                .build();

        when(deliveryOutboxJpaRepository.findByTypeAndOutboxStatus("OrderDelivery", OutboxStatus.STARTED))
                .thenReturn(Optional.of(List.of(entity)));

        Optional<List<DeliveryOutboxMessage>> result =
                repository.findByTypeAndOutboxStatus("OrderDelivery", OutboxStatus.STARTED);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals(id, result.get().get(0).getId());
    }
}
