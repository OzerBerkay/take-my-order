package com.berkay.order.service.domain.review;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.domain.dto.restaurant.RestaurantRatingCalculationResult;
import com.berkay.order.service.domain.dto.review.DeactivateOrderReviewCommand;
import com.berkay.order.service.domain.dto.review.DeactivateOrderReviewResponse;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.exception.ReviewNotFoundException;
import com.berkay.order.service.domain.mapper.OrderReviewDataMapper;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingEventPayload;
import com.berkay.order.service.domain.outbox.scheduler.rating.RestaurantRatingOutboxHelper;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import com.berkay.order.service.domain.valueobject.OrderReviewId;
import com.berkay.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeactivateOrderReviewCommandHandlerTest {

    @Mock
    private OrderReviewRepository orderReviewRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private OrderReviewDataMapper orderReviewDataMapper;
    @Mock
    private RestaurantRatingOutboxHelper ratingOutboxHelper;

    private DeactivateOrderReviewCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeactivateOrderReviewCommandHandler(
                orderReviewRepository,
                restaurantRepository,
                orderReviewDataMapper,
                ratingOutboxHelper
        );
    }

    @Test
    @DisplayName("Should successfully deactivate review and recalculate restaurant rating")
    void testDeactivateReviewSuccess() {
        UUID reviewId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();

        DeactivateOrderReviewCommand command = DeactivateOrderReviewCommand.builder()
                .reviewId(reviewId)
                .restaurantId(restaurantId)
                .moderatorId(moderatorId)
                .reason("Hakaret icerikli yorum")
                .build();

        OrderReview review = mock(OrderReview.class);
        when(review.getRestaurantId()).thenReturn(new RestaurantId(restaurantId));
        when(orderReviewRepository.findById(new OrderReviewId(reviewId))).thenReturn(Optional.of(review));

        Restaurant restaurant = mock(Restaurant.class);
        when(restaurantRepository.findRestaurantByIdWithLock(restaurantId)).thenReturn(Optional.of(restaurant));

        when(orderReviewRepository.save(review)).thenReturn(review);

        RestaurantRatingCalculationResult calcResult = new RestaurantRatingCalculationResult(5L, 4.2);
        when(orderReviewRepository.calculateRatingAndCount(restaurantId)).thenReturn(calcResult);

        DeactivateOrderReviewResponse expectedResponse = DeactivateOrderReviewResponse.builder()
                .reviewId(reviewId)
                .message("Yorum başarıyla deaktif edildi ve yayından kaldırıldı.")
                .build();
        when(orderReviewDataMapper.orderReviewToDeactivateOrderReviewResponse(eq(review), anyString()))
                .thenReturn(expectedResponse);

        DeactivateOrderReviewResponse response = handler.deactivateReview(command);

        assertNotNull(response);
        assertEquals(reviewId, response.getReviewId());

        verify(review).deactivate("Hakaret icerikli yorum", moderatorId);
        verify(orderReviewRepository).save(review);

        ArgumentCaptor<RestaurantRatingEventPayload> payloadCaptor = ArgumentCaptor.forClass(RestaurantRatingEventPayload.class);
        verify(ratingOutboxHelper).saveRatingOutboxMessage(payloadCaptor.capture(), eq(OutboxStatus.STARTED));

        RestaurantRatingEventPayload capturedPayload = payloadCaptor.getValue();
        assertEquals(restaurantId.toString(), capturedPayload.getRestaurantId());
        assertEquals(4.2, capturedPayload.getAverageRating());
        assertEquals(5, capturedPayload.getTotalReviews());
    }

    @Test
    @DisplayName("Should publish NULL averageRating and 0 totalReviews when the last active review is deactivated")
    void testDeactivateLastReviewYieldsNullAverageRating() {
        UUID reviewId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();

        DeactivateOrderReviewCommand command = DeactivateOrderReviewCommand.builder()
                .reviewId(reviewId)
                .restaurantId(restaurantId)
                .moderatorId(moderatorId)
                .reason("Ihlal")
                .build();

        OrderReview review = mock(OrderReview.class);
        when(review.getRestaurantId()).thenReturn(new RestaurantId(restaurantId));
        when(orderReviewRepository.findById(new OrderReviewId(reviewId))).thenReturn(Optional.of(review));
        when(restaurantRepository.findRestaurantByIdWithLock(restaurantId)).thenReturn(Optional.of(mock(Restaurant.class)));
        when(orderReviewRepository.save(review)).thenReturn(review);

        // When 0 reviews remain, average rating is null
        RestaurantRatingCalculationResult calcResult = new RestaurantRatingCalculationResult(0L, null);
        when(orderReviewRepository.calculateRatingAndCount(restaurantId)).thenReturn(calcResult);

        DeactivateOrderReviewResponse expectedResponse = DeactivateOrderReviewResponse.builder()
                .reviewId(reviewId)
                .build();
        when(orderReviewDataMapper.orderReviewToDeactivateOrderReviewResponse(eq(review), anyString()))
                .thenReturn(expectedResponse);

        handler.deactivateReview(command);

        ArgumentCaptor<RestaurantRatingEventPayload> payloadCaptor = ArgumentCaptor.forClass(RestaurantRatingEventPayload.class);
        verify(ratingOutboxHelper).saveRatingOutboxMessage(payloadCaptor.capture(), eq(OutboxStatus.STARTED));

        RestaurantRatingEventPayload capturedPayload = payloadCaptor.getValue();
        assertNull(capturedPayload.getAverageRating());
        assertEquals(0, capturedPayload.getTotalReviews());
    }

    @Test
    @DisplayName("Should throw ReviewNotFoundException when review does not exist")
    void testDeactivateReviewNotFound() {
        UUID reviewId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        DeactivateOrderReviewCommand command = DeactivateOrderReviewCommand.builder()
                .reviewId(reviewId)
                .restaurantId(restaurantId)
                .reason("Ihlal")
                .build();

        when(orderReviewRepository.findById(new OrderReviewId(reviewId))).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> handler.deactivateReview(command));
    }

    @Test
    @DisplayName("Should throw OrderDomainException when review belongs to different restaurant")
    void testDeactivateReviewWrongRestaurant() {
        UUID reviewId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID otherRestaurantId = UUID.randomUUID();

        DeactivateOrderReviewCommand command = DeactivateOrderReviewCommand.builder()
                .reviewId(reviewId)
                .restaurantId(restaurantId)
                .reason("Ihlal")
                .build();

        OrderReview review = mock(OrderReview.class);
        when(review.getRestaurantId()).thenReturn(new RestaurantId(otherRestaurantId));
        when(orderReviewRepository.findById(new OrderReviewId(reviewId))).thenReturn(Optional.of(review));

        assertThrows(OrderDomainException.class, () -> handler.deactivateReview(command));
    }
}
