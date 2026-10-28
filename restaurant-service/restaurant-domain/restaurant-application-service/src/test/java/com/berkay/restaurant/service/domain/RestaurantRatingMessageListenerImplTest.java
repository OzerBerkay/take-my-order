package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.message.RestaurantRatingUpdateModel;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantRatingMessageListenerImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantRatingMessageListenerImpl ratingMessageListener;

    @Test
    @DisplayName("Should successfully update restaurant rating with 1 decimal scale")
    void shouldUpdateRatingSuccessfully() {
        UUID restaurantId = UUID.randomUUID();
        RestaurantRatingUpdateModel model = RestaurantRatingUpdateModel.builder()
                .restaurantId(restaurantId.toString())
                .averageRating(4.67)
                .totalReviews(25)
                .updatedAt(Instant.now())
                .build();

        when(restaurantRepository.updateRestaurantRating(eq(restaurantId), any(BigDecimal.class), eq(25), any(ZonedDateTime.class)))
                .thenReturn(1);

        ratingMessageListener.updateRating(model);

        verify(restaurantRepository, times(1))
                .updateRestaurantRating(eq(restaurantId), eq(new BigDecimal("4.7")), eq(25), any(ZonedDateTime.class));
    }

    @Test
    @DisplayName("Should update rating with null averageRating when all reviews are deleted or count is 0")
    void shouldUpdateRatingWithNullWhenAverageRatingIsNull() {
        UUID restaurantId = UUID.randomUUID();
        RestaurantRatingUpdateModel model = RestaurantRatingUpdateModel.builder()
                .restaurantId(restaurantId.toString())
                .averageRating(null)
                .totalReviews(0)
                .updatedAt(Instant.now())
                .build();

        when(restaurantRepository.updateRestaurantRating(eq(restaurantId), isNull(), eq(0), any(ZonedDateTime.class)))
                .thenReturn(1);

        ratingMessageListener.updateRating(model);

        verify(restaurantRepository, times(1))
                .updateRestaurantRating(eq(restaurantId), isNull(), eq(0), any(ZonedDateTime.class));
    }

    @Test
    @DisplayName("Should discard stale event silently when repository reports 0 updated rows")
    void shouldDiscardStaleEventSilentlyWhenUpdatedRowsIsZero() {
        UUID restaurantId = UUID.randomUUID();
        RestaurantRatingUpdateModel model = RestaurantRatingUpdateModel.builder()
                .restaurantId(restaurantId.toString())
                .averageRating(3.5)
                .totalReviews(10)
                .updatedAt(Instant.now().minusSeconds(3600))
                .build();

        when(restaurantRepository.updateRestaurantRating(eq(restaurantId), any(BigDecimal.class), eq(10), any(ZonedDateTime.class)))
                .thenReturn(0);

        ratingMessageListener.updateRating(model);

        verify(restaurantRepository, times(1))
                .updateRestaurantRating(eq(restaurantId), any(BigDecimal.class), eq(10), any(ZonedDateTime.class));
    }

    @Test
    @DisplayName("Should skip processing when model is null or restaurantId is null")
    void shouldSkipWhenModelOrRestaurantIdIsNull() {
        ratingMessageListener.updateRating(null);

        RestaurantRatingUpdateModel invalidModel = RestaurantRatingUpdateModel.builder()
                .restaurantId(null)
                .build();
        ratingMessageListener.updateRating(invalidModel);

        verify(restaurantRepository, never())
                .updateRestaurantRating(any(), any(), any(), any());
    }
}
