package com.berkay.restaurant.service.domain.outbox.scheduler;

import com.berkay.outbox.OutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import com.berkay.restaurant.service.domain.ports.output.RestaurantCreatedMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestaurantOutboxScheduler implements OutboxScheduler {

    private final RestaurantOutboxHelper restaurantOutboxHelper;
    private final RestaurantCreatedMessagePublisher restaurantCreatedMessagePublisher;

    public RestaurantOutboxScheduler(RestaurantOutboxHelper restaurantOutboxHelper,
                                     RestaurantCreatedMessagePublisher restaurantCreatedMessagePublisher) {
        this.restaurantOutboxHelper = restaurantOutboxHelper;
        this.restaurantCreatedMessagePublisher = restaurantCreatedMessagePublisher;
    }

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${restaurant-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${restaurant-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<RestaurantOutboxMessage>> outboxMessagesResponse =
                restaurantOutboxHelper.getRestaurantOutboxMessageByOutboxStatus(OutboxStatus.STARTED);

        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<RestaurantOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} RestaurantOutboxMessage with id: {}, sending to message bus!",
                    outboxMessages.size(),
                    outboxMessages.stream().map(message -> message.getId().toString())
                            .collect(Collectors.joining(",")));

            outboxMessages.forEach(outboxMessage ->
                    restaurantCreatedMessagePublisher.publish(outboxMessage, this::updateOutboxStatus));

            log.info("{} RestaurantOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }

    @Transactional
    public void updateOutboxStatus(RestaurantOutboxMessage restaurantOutboxMessage, OutboxStatus outboxStatus) {
        restaurantOutboxHelper.updateOutboxMessage(restaurantOutboxMessage, outboxStatus);
    }
}
