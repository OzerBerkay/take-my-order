package com.berkay.restaurant.service.domain.outbox.scheduler;

import com.berkay.outbox.OutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import com.berkay.restaurant.service.domain.RestaurantPersonnelMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestaurantPersonnelOutboxScheduler implements OutboxScheduler {

    private final RestaurantOutboxHelper restaurantOutboxHelper;
    private final RestaurantPersonnelMessagePublisher restaurantPersonnelMessagePublisher;

    public RestaurantPersonnelOutboxScheduler(RestaurantOutboxHelper restaurantOutboxHelper,
                                              RestaurantPersonnelMessagePublisher restaurantPersonnelMessagePublisher) {
        this.restaurantOutboxHelper = restaurantOutboxHelper;
        this.restaurantPersonnelMessagePublisher = restaurantPersonnelMessagePublisher;
    }

    @Override
    @Scheduled(fixedDelayString = "${restaurant-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${restaurant-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        List<RestaurantOutboxMessage> outboxMessages =
                restaurantOutboxHelper.getPersonnelOutboxMessageByOutboxStatus(OutboxStatus.STARTED);

        if (!outboxMessages.isEmpty()) {
            log.info("Received {} RestaurantPersonnelOutboxMessage with id: {}, sending to message bus!",
                    outboxMessages.size(),
                    outboxMessages.stream().map(message -> message.getId().toString())
                            .collect(Collectors.joining(",")));

            outboxMessages.forEach(outboxMessage ->
                    restaurantPersonnelMessagePublisher.publish(outboxMessage, restaurantOutboxHelper::updateOutboxMessage));

            log.info("{} RestaurantPersonnelOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }
}
