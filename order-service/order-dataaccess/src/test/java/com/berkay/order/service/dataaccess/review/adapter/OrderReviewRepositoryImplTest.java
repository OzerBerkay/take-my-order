package com.berkay.order.service.dataaccess.review.adapter;

import com.berkay.order.service.dataaccess.review.entity.OrderReviewEntity;
import com.berkay.order.service.dataaccess.review.mapper.OrderReviewDataAccessMapper;
import com.berkay.order.service.dataaccess.review.repository.OrderReviewJpaRepository;
import com.berkay.order.service.domain.dto.review.OrderReviewPageResult;
import com.berkay.order.service.domain.dto.review.RestaurantReviewStatistics;
import com.berkay.order.service.domain.entity.OrderReview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderReviewRepositoryImplTest {

    private OrderReviewJpaRepository repository;
    private OrderReviewDataAccessMapper mapper;
    private OrderReviewRepositoryImpl orderReviewRepository;

    @BeforeEach
    void setUp() {
        repository = mock(OrderReviewJpaRepository.class);
        mapper = mock(OrderReviewDataAccessMapper.class);
        orderReviewRepository = new OrderReviewRepositoryImpl(repository, mapper);
    }

    @Test
    void testFindActiveByRestaurantIdAndStatus_All() {
        UUID restaurantId = UUID.randomUUID();
        OrderReviewEntity entity = new OrderReviewEntity();
        Page<OrderReviewEntity> page = new PageImpl<>(List.of(entity));

        when(repository.findByRestaurantIdAndIsActiveTrue(eq(restaurantId), any(Pageable.class))).thenReturn(page);
        when(mapper.orderReviewEntityToOrderReview(entity)).thenReturn(OrderReview.builder().build());

        OrderReviewPageResult result = orderReviewRepository.findActiveByRestaurantIdAndStatus(restaurantId, "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findByRestaurantIdAndIsActiveTrue(eq(restaurantId), any(Pageable.class));
    }

    @Test
    void testFindActiveByRestaurantIdAndStatus_Replied() {
        UUID restaurantId = UUID.randomUUID();
        OrderReviewEntity entity = new OrderReviewEntity();
        Page<OrderReviewEntity> page = new PageImpl<>(List.of(entity));

        when(repository.findByRestaurantIdAndIsActiveTrueAndRestaurantReplyIsNotNull(eq(restaurantId), any(Pageable.class))).thenReturn(page);
        when(mapper.orderReviewEntityToOrderReview(entity)).thenReturn(OrderReview.builder().build());

        OrderReviewPageResult result = orderReviewRepository.findActiveByRestaurantIdAndStatus(restaurantId, "REPLIED", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findByRestaurantIdAndIsActiveTrueAndRestaurantReplyIsNotNull(eq(restaurantId), any(Pageable.class));
    }

    @Test
    void testFindActiveByRestaurantIdAndStatus_Pending() {
        UUID restaurantId = UUID.randomUUID();
        OrderReviewEntity entity = new OrderReviewEntity();
        Page<OrderReviewEntity> page = new PageImpl<>(List.of(entity));

        when(repository.findByRestaurantIdAndIsActiveTrueAndRestaurantReplyIsNullAndCreatedAtGreaterThanEqual(
                eq(restaurantId), any(ZonedDateTime.class), any(Pageable.class))).thenReturn(page);
        when(mapper.orderReviewEntityToOrderReview(entity)).thenReturn(OrderReview.builder().build());

        OrderReviewPageResult result = orderReviewRepository.findActiveByRestaurantIdAndStatus(restaurantId, "PENDING", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findByRestaurantIdAndIsActiveTrueAndRestaurantReplyIsNullAndCreatedAtGreaterThanEqual(
                eq(restaurantId), any(ZonedDateTime.class), any(Pageable.class));
    }

    @Test
    void testGetReviewStatistics() {
        UUID restaurantId = UUID.randomUUID();
        Object[] row = new Object[]{10L, 6L, 3L};
        when(repository.getReviewStatistics(eq(restaurantId), any(ZonedDateTime.class)))
                .thenReturn(Collections.singletonList(row));

        RestaurantReviewStatistics stats = orderReviewRepository.getReviewStatistics(restaurantId);

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalElements());
        assertEquals(6L, stats.getRepliedCount());
        assertEquals(3L, stats.getPendingReplyCount());
    }

    @Test
    void testGetReviewStatistics_Empty() {
        UUID restaurantId = UUID.randomUUID();
        when(repository.getReviewStatistics(eq(restaurantId), any(ZonedDateTime.class)))
                .thenReturn(Collections.emptyList());

        RestaurantReviewStatistics stats = orderReviewRepository.getReviewStatistics(restaurantId);

        assertNotNull(stats);
        assertEquals(0L, stats.getTotalElements());
        assertEquals(0L, stats.getRepliedCount());
        assertEquals(0L, stats.getPendingReplyCount());
    }
}
