package com.berkay.order.service.domain;

import com.berkay.order.service.application.OrderServiceApplication;
import com.berkay.order.service.dataaccess.outbox.restaurantrating.repository.RestaurantRatingOutboxJpaRepository;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import com.berkay.order.service.domain.outbox.scheduler.rating.RestaurantRatingOutboxHelper;
import com.berkay.order.service.domain.outbox.scheduler.rating.RestaurantRatingOutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = OrderServiceApplication.class,
        properties = {
                "ORDER_SERVICE_PORT=8199",
                "order-service.restaurant-rating-updated-topic-name=restaurant-rating-updated",
                "order-service.restaurant-created-topic-name=restaurant-created",
                "order-service.payment-response-topic-name=payment-response",
                "order-service.payment-request-topic-name=payment-request",
                "order-service.restaurant-approval-request-topic-name=restaurant-approval-request",
                "order-service.restaurant-approval-response-topic-name=restaurant-approval-response",
                "kafka-consumer-config.restaurant-created-consumer-group-id=restaurant-created-topic-consumer-test-rating",
                "kafka-consumer-config.payment-consumer-group-id=payment-topic-consumer-test-rating",
                "kafka-consumer-config.restaurant-approval-consumer-group-id=restaurant-approval-topic-consumer-test-rating"
        }
)
public class RestaurantRatingOutboxIntegrationTest {

    @Autowired
    private RestaurantRatingOutboxJpaRepository repository;

    @Autowired
    private RestaurantRatingOutboxHelper helper;

    @Autowired
    private RestaurantRatingOutboxScheduler scheduler;

    @Test
    @DisplayName("Verify rating outbox repository and helper can read STARTED messages from DB")
    void testReadStartedMessages() {
        assertNotNull(repository, "repository bean must not be null");
        assertNotNull(helper, "helper bean must not be null");
        assertNotNull(scheduler, "scheduler bean must not be null");

        Optional<List<RestaurantRatingOutboxMessage>> messages =
                helper.getRestaurantRatingOutboxMessagesByOutboxStatus(OutboxStatus.STARTED);

        assertTrue(messages.isPresent(), "Messages Optional must be present");
        log.info("Found {} messages with STARTED status", messages.get().size());

        assertDoesNotThrow(() -> scheduler.processOutboxMessage());
        log.info("scheduler.processOutboxMessage() executed without errors");
    }
}
