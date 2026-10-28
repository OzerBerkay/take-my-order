package com.berkay.order.service.domain.outbox.scheduler.rating;

import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantRatingOutboxCleanerSchedulerTest {

    @Mock
    private RestaurantRatingOutboxHelper ratingOutboxHelper;

    private RestaurantRatingOutboxCleanerScheduler cleanerScheduler;

    @BeforeEach
    void setUp() {
        cleanerScheduler = new RestaurantRatingOutboxCleanerScheduler(ratingOutboxHelper);
    }

    @Test
    @DisplayName("When COMPLETED rating outbox messages exist, deletes them")
    void testProcessOutboxMessageWhenPresent() {
        RestaurantRatingOutboxMessage message = RestaurantRatingOutboxMessage.builder()
                .id(UUID.randomUUID())
                .restaurantId(UUID.randomUUID())
                .createdAt(ZonedDateTime.now())
                .type(RestaurantRatingOutboxHelper.RESTAURANT_RATING_EVENT_TYPE)
                .payload("{}")
                .outboxStatus(OutboxStatus.COMPLETED)
                .build();

        when(ratingOutboxHelper.getRestaurantRatingOutboxMessagesByOutboxStatus(OutboxStatus.COMPLETED))
                .thenReturn(Optional.of(List.of(message)));

        cleanerScheduler.processOutboxMessage();

        verify(ratingOutboxHelper, times(1)).deleteRestaurantRatingOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
    }

    @Test
    @DisplayName("When no COMPLETED messages exist, does nothing")
    void testProcessOutboxMessageWhenEmpty() {
        when(ratingOutboxHelper.getRestaurantRatingOutboxMessagesByOutboxStatus(OutboxStatus.COMPLETED))
                .thenReturn(Optional.empty());

        cleanerScheduler.processOutboxMessage();

        verify(ratingOutboxHelper, never()).deleteRestaurantRatingOutboxMessageByOutboxStatus(any());
    }
}
