package com.berkay.order.service.domain.outbox.scheduler.delivery;

import com.berkay.order.service.domain.outbox.model.delivery.DeliveryOutboxMessage;
import com.berkay.order.service.domain.ports.output.message.publisher.delivery.DeliveryMessagePublisher;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryOutboxSchedulerTest {

    @Mock
    private DeliveryOutboxHelper deliveryOutboxHelper;

    @Mock
    private DeliveryMessagePublisher deliveryMessagePublisher;

    private DeliveryOutboxScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new DeliveryOutboxScheduler(deliveryOutboxHelper, deliveryMessagePublisher);
    }

    @Test
    void processOutboxMessage_WhenMessagesPresent_PublishesMessages() {
        DeliveryOutboxMessage message = DeliveryOutboxMessage.builder()
                .id(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .createdAt(ZonedDateTime.now())
                .type(DeliveryOutboxHelper.ORDER_DELIVERY_EVENT_TYPE)
                .payload("{}")
                .outboxStatus(OutboxStatus.STARTED)
                .build();

        when(deliveryOutboxHelper.getDeliveryOutboxMessagesByOutboxStatus(OutboxStatus.STARTED))
                .thenReturn(Optional.of(List.of(message)));

        scheduler.processOutboxMessage();

        verify(deliveryMessagePublisher).publish(eq(message), any());
    }

    @Test
    void processOutboxMessage_WhenEmpty_DoesNothing() {
        when(deliveryOutboxHelper.getDeliveryOutboxMessagesByOutboxStatus(OutboxStatus.STARTED))
                .thenReturn(Optional.empty());

        scheduler.processOutboxMessage();

        verify(deliveryMessagePublisher, never()).publish(any(), any());
    }
}
