package com.berkay.order.service.domain.outbox.scheduler.rating;

import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import com.berkay.order.service.domain.ports.output.message.publisher.rating.RestaurantRatingMessagePublisher;
import com.berkay.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
public class RestaurantRatingOutboxSchedulerTest {

    @Mock
    private RestaurantRatingOutboxHelper ratingOutboxHelper;

    @Mock
    private RestaurantRatingMessagePublisher ratingMessagePublisher;

    private RestaurantRatingOutboxScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new RestaurantRatingOutboxScheduler(ratingOutboxHelper, ratingMessagePublisher);
    }

    @Test
    @DisplayName("When STARTED rating outbox messages exist, publishes to Kafka")
    void testProcessOutboxMessageWhenPresent() {
        RestaurantRatingOutboxMessage message = RestaurantRatingOutboxMessage.builder()
                .id(UUID.randomUUID())
                .restaurantId(UUID.randomUUID())
                .createdAt(ZonedDateTime.now())
                .type(RestaurantRatingOutboxHelper.RESTAURANT_RATING_EVENT_TYPE)
                .payload("{}")
                .outboxStatus(OutboxStatus.STARTED)
                .build();

        when(ratingOutboxHelper.getRestaurantRatingOutboxMessagesByOutboxStatus(OutboxStatus.STARTED))
                .thenReturn(Optional.of(List.of(message)));

        scheduler.processOutboxMessage();

        verify(ratingMessagePublisher, times(1)).publish(eq(message), any());
    }

    @Test
    @DisplayName("When no messages exist, does not publish")
    void testProcessOutboxMessageWhenEmpty() {
        when(ratingOutboxHelper.getRestaurantRatingOutboxMessagesByOutboxStatus(OutboxStatus.STARTED))
                .thenReturn(Optional.empty());

        scheduler.processOutboxMessage();

        verify(ratingMessagePublisher, never()).publish(any(), any());
    }
}
