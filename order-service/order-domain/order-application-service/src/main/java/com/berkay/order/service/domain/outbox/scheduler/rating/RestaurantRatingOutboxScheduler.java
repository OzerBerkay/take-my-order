package com.berkay.order.service.domain.outbox.scheduler.rating;

import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import com.berkay.order.service.domain.ports.output.message.publisher.rating.RestaurantRatingMessagePublisher;
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
public class RestaurantRatingOutboxScheduler implements OutboxScheduler {

    private final RestaurantRatingOutboxHelper ratingOutboxHelper;
    private final RestaurantRatingMessagePublisher ratingMessagePublisher;

    public RestaurantRatingOutboxScheduler(RestaurantRatingOutboxHelper ratingOutboxHelper,
                                           RestaurantRatingMessagePublisher ratingMessagePublisher) {
        this.ratingOutboxHelper = ratingOutboxHelper;
        this.ratingMessagePublisher = ratingMessagePublisher;
    }

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<RestaurantRatingOutboxMessage>> outboxMessagesResponse =
                ratingOutboxHelper.getRestaurantRatingOutboxMessagesByOutboxStatus(OutboxStatus.STARTED);

        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<RestaurantRatingOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} RestaurantRatingOutboxMessage with ids: {}, sending to Kafka!",
                    outboxMessages.size(),
                    outboxMessages.stream().map(m -> m.getId().toString()).collect(Collectors.joining(",")));

            outboxMessages.forEach(outboxMessage ->
                    ratingMessagePublisher.publish(outboxMessage, this::updateOutboxStatus));
            log.info("{} RestaurantRatingOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }

    private void updateOutboxStatus(RestaurantRatingOutboxMessage outboxMessage, OutboxStatus outboxStatus) {
        outboxMessage.setOutboxStatus(outboxStatus);
        outboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        ratingOutboxHelper.save(outboxMessage);
        log.info("RestaurantRatingOutboxMessage is updated with outbox status: {}", outboxStatus.name());
    }
}
