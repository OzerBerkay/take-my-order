package com.berkay.restaurant.service.domain.outbox.scheduler;

import com.berkay.outbox.OutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class RestaurantPersonnelOutboxCleanerScheduler implements OutboxScheduler {

    private final RestaurantOutboxHelper restaurantOutboxHelper;

    public RestaurantPersonnelOutboxCleanerScheduler(RestaurantOutboxHelper restaurantOutboxHelper) {
        this.restaurantOutboxHelper = restaurantOutboxHelper;
    }

    @Transactional
    @Scheduled(cron = "@midnight") // Her gece yarısı çalışır
    @Override
    public void processOutboxMessage() {
        List<RestaurantOutboxMessage> outboxMessages =
                restaurantOutboxHelper.getPersonnelOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);

        if (!outboxMessages.isEmpty()) {
            log.info("Received {} RestaurantPersonnelOutboxMessage for clean-up!", outboxMessages.size());

            restaurantOutboxHelper.deletePersonnelOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);

            log.info("Deleted {} RestaurantPersonnelOutboxMessage!", outboxMessages.size());
        }
    }
}
