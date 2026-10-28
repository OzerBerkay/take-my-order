package com.berkay.order.service.domain.review;

import com.berkay.order.service.domain.dto.review.*;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.mapper.OrderReviewDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRestaurantReviewsQueryHandlerTest {

    @Mock
    private OrderReviewRepository orderReviewRepository;

    private OrderReviewDataMapper orderReviewDataMapper;

    private GetRestaurantReviewsQueryHandler queryHandler;

    @BeforeEach
    void setUp() {
        orderReviewDataMapper = new OrderReviewDataMapper();
        queryHandler = new GetRestaurantReviewsQueryHandler(orderReviewRepository, orderReviewDataMapper);
    }

    @Test
    @DisplayName("Should fetch reviews with ALL status and map statistics correctly")
    void shouldFetchReviewsWithAllStatus() {
        UUID restaurantId = UUID.randomUUID();
        GetRestaurantReviewsQuery query = GetRestaurantReviewsQuery.builder()
                .restaurantId(restaurantId)
                .status("ALL")
                .page(0)
                .size(10)
                .build();

        OrderReviewPageResult pageResult = OrderReviewPageResult.builder()
                .reviews(Collections.emptyList())
                .page(0)
                .size(10)
                .totalElements(5L)
                .totalPages(1)
                .isLast(true)
                .build();

        RestaurantReviewStatistics statistics = RestaurantReviewStatistics.builder()
                .totalElements(5L)
                .repliedCount(3L)
                .pendingReplyCount(2L)
                .build();

        when(orderReviewRepository.findActiveByRestaurantIdAndStatus(eq(restaurantId), eq("ALL"), eq(0), eq(10)))
                .thenReturn(pageResult);
        when(orderReviewRepository.getReviewStatistics(eq(restaurantId)))
                .thenReturn(statistics);

        GetRestaurantReviewsResponse response = queryHandler.getRestaurantReviews(query);

        assertNotNull(response);
        assertEquals(5L, response.getTotalElements());
        assertEquals(3L, response.getRepliedCount());
        assertEquals(2L, response.getPendingReplyCount());
        verify(orderReviewRepository).findActiveByRestaurantIdAndStatus(restaurantId, "ALL", 0, 10);
        verify(orderReviewRepository).getReviewStatistics(restaurantId);
    }

    @Test
    @DisplayName("Should fetch reviews with PENDING status filter")
    void shouldFetchReviewsWithPendingStatus() {
        UUID restaurantId = UUID.randomUUID();
        GetRestaurantReviewsQuery query = GetRestaurantReviewsQuery.builder()
                .restaurantId(restaurantId)
                .status("PENDING")
                .page(0)
                .size(10)
                .build();

        OrderReviewPageResult pageResult = OrderReviewPageResult.builder()
                .reviews(Collections.emptyList())
                .page(0)
                .size(10)
                .totalElements(2L)
                .totalPages(1)
                .isLast(true)
                .build();

        RestaurantReviewStatistics statistics = RestaurantReviewStatistics.builder()
                .totalElements(5L)
                .repliedCount(3L)
                .pendingReplyCount(2L)
                .build();

        when(orderReviewRepository.findActiveByRestaurantIdAndStatus(eq(restaurantId), eq("PENDING"), eq(0), eq(10)))
                .thenReturn(pageResult);
        when(orderReviewRepository.getReviewStatistics(eq(restaurantId)))
                .thenReturn(statistics);

        GetRestaurantReviewsResponse response = queryHandler.getRestaurantReviews(query);

        assertNotNull(response);
        assertEquals(2L, response.getTotalElements());
        assertEquals(3L, response.getRepliedCount());
        assertEquals(2L, response.getPendingReplyCount());
    }
}
