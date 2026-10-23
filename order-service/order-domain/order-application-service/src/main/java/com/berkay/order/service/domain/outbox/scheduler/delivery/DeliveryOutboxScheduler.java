package com.berkay.order.service.domain.outbox.scheduler.delivery;

import com.berkay.order.service.domain.outbox.model.delivery.DeliveryOutboxMessage;
import com.berkay.order.service.domain.ports.output.message.publisher.delivery.DeliveryMessagePublisher;
import com.berkay.outbox.OutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DeliveryOutboxScheduler implements OutboxScheduler {

    private final DeliveryOutboxHelper deliveryOutboxHelper;
    private final DeliveryMessagePublisher deliveryMessagePublisher;

    public DeliveryOutboxScheduler(DeliveryOutboxHelper deliveryOutboxHelper,
                                   DeliveryMessagePublisher deliveryMessagePublisher) {
        this.deliveryOutboxHelper = deliveryOutboxHelper;
        this.deliveryMessagePublisher = deliveryMessagePublisher;
    }

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<DeliveryOutboxMessage>> outboxMessagesResponse =
                deliveryOutboxHelper.getDeliveryOutboxMessagesByOutboxStatus(OutboxStatus.STARTED);

        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<DeliveryOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} DeliveryOutboxMessage with ids: {}, sending to message bus!",
                    outboxMessages.size(),
                    outboxMessages.stream().map(m -> m.getId().toString()).collect(Collectors.joining(",")));

            outboxMessages.forEach(outboxMessage ->
                    deliveryMessagePublisher.publish(outboxMessage, this::updateOutboxStatus));
            log.info("{} DeliveryOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }

    private void updateOutboxStatus(DeliveryOutboxMessage deliveryOutboxMessage, OutboxStatus outboxStatus) {
        deliveryOutboxMessage.setOutboxStatus(outboxStatus);
        deliveryOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        deliveryOutboxHelper.save(deliveryOutboxMessage);
        log.info("DeliveryOutboxMessage is updated with outbox status: {}", outboxStatus.name());
    }
}
