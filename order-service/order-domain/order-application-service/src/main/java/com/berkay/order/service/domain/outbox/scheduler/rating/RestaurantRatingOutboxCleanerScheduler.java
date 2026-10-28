package com.berkay.order.service.domain.outbox.scheduler.rating;

import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import com.berkay.outbox.OutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RestaurantRatingOutboxCleanerScheduler implements OutboxScheduler {

    private final RestaurantRatingOutboxHelper ratingOutboxHelper;

    public RestaurantRatingOutboxCleanerScheduler(RestaurantRatingOutboxHelper ratingOutboxHelper) {
        this.ratingOutboxHelper = ratingOutboxHelper;
    }

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<RestaurantRatingOutboxMessage>> outboxMessagesResponse =
                ratingOutboxHelper.getRestaurantRatingOutboxMessagesByOutboxStatus(OutboxStatus.COMPLETED);

        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<RestaurantRatingOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} RestaurantRatingOutboxMessage for clean-up", outboxMessages.size());
            ratingOutboxHelper.deleteRestaurantRatingOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
            log.info("{} RestaurantRatingOutboxMessage deleted!", outboxMessages.size());
        }
    }
}
