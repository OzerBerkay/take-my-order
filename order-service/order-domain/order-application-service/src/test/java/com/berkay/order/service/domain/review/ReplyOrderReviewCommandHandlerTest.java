package com.berkay.order.service.domain.review;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.domain.dto.review.ReplyOrderReviewCommand;
import com.berkay.order.service.domain.dto.review.ReplyOrderReviewResponse;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.exception.ReviewNotFoundException;
import com.berkay.order.service.domain.mapper.OrderReviewDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import com.berkay.order.service.domain.valueobject.OrderReviewId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplyOrderReviewCommandHandlerTest {

    @Mock
    private OrderReviewRepository orderReviewRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private OrderReviewDataMapper orderReviewDataMapper;

    private ReplyOrderReviewCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReplyOrderReviewCommandHandler(
                orderReviewRepository,
                restaurantRepository,
                orderReviewDataMapper
        );
    }

    @Test
    @DisplayName("Should successfully reply to review when restaurant is active and review belongs to restaurant")
    void testReplyReviewSuccess() {
        UUID reviewId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        ReplyOrderReviewCommand command = ReplyOrderReviewCommand.builder()
                .reviewId(reviewId)
                .restaurantId(restaurantId)
                .reply("Afiyet olsun!")
                .build();

        OrderReview review = mock(OrderReview.class);
        when(review.getRestaurantId()).thenReturn(new RestaurantId(restaurantId));
        when(orderReviewRepository.findById(new OrderReviewId(reviewId))).thenReturn(Optional.of(review));

        Restaurant restaurant = mock(Restaurant.class);
        when(restaurant.isActive()).thenReturn(true);
        when(restaurantRepository.findRestaurantByRestaurantId(restaurantId)).thenReturn(Optional.of(restaurant));

        when(orderReviewRepository.save(review)).thenReturn(review);

        ReplyOrderReviewResponse expectedResponse = ReplyOrderReviewResponse.builder()
                .reviewId(reviewId)
                .message("Yorum yanıtınız başarıyla kaydedildi.")
                .build();
        when(orderReviewDataMapper.orderReviewToReplyOrderReviewResponse(eq(review), anyString()))
                .thenReturn(expectedResponse);

        ReplyOrderReviewResponse response = handler.replyReview(command);

        assertNotNull(response);
        assertEquals(reviewId, response.getReviewId());

        verify(review).reply("Afiyet olsun!");
        verify(orderReviewRepository).save(review);
    }

    @Test
    @DisplayName("Should throw ReviewNotFoundException when review is not found")
    void testReplyReviewNotFound() {
        UUID reviewId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        ReplyOrderReviewCommand command = ReplyOrderReviewCommand.builder()
                .reviewId(reviewId)
                .restaurantId(restaurantId)
                .reply("Afiyet olsun!")
                .build();

        when(orderReviewRepository.findById(new OrderReviewId(reviewId))).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> handler.replyReview(command));
    }

    @Test
    @DisplayName("Should throw OrderDomainException when review does not belong to specified restaurant")
    void testReplyReviewWrongRestaurant() {
        UUID reviewId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID differentRestaurantId = UUID.randomUUID();

        ReplyOrderReviewCommand command = ReplyOrderReviewCommand.builder()
                .reviewId(reviewId)
                .restaurantId(restaurantId)
                .reply("Afiyet olsun!")
                .build();

        OrderReview review = mock(OrderReview.class);
        when(review.getRestaurantId()).thenReturn(new RestaurantId(differentRestaurantId));
        when(orderReviewRepository.findById(new OrderReviewId(reviewId))).thenReturn(Optional.of(review));

        assertThrows(OrderDomainException.class, () -> handler.replyReview(command));
    }

    @Test
    @DisplayName("Should throw OrderDomainException when restaurant is inactive")
    void testReplyReviewInactiveRestaurant() {
        UUID reviewId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        ReplyOrderReviewCommand command = ReplyOrderReviewCommand.builder()
                .reviewId(reviewId)
                .restaurantId(restaurantId)
                .reply("Afiyet olsun!")
                .build();

        OrderReview review = mock(OrderReview.class);
        when(review.getRestaurantId()).thenReturn(new RestaurantId(restaurantId));
        when(orderReviewRepository.findById(new OrderReviewId(reviewId))).thenReturn(Optional.of(review));

        Restaurant restaurant = mock(Restaurant.class);
        when(restaurant.isActive()).thenReturn(false);
        when(restaurantRepository.findRestaurantByRestaurantId(restaurantId)).thenReturn(Optional.of(restaurant));

        assertThrows(OrderDomainException.class, () -> handler.replyReview(command));
    }
}
