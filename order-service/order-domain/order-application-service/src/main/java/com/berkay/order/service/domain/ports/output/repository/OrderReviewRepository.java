package com.berkay.order.service.domain.ports.output.repository;

import com.berkay.order.service.domain.dto.restaurant.RestaurantRatingCalculationResult;
import com.berkay.order.service.domain.dto.review.OrderReviewPageResult;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.valueobject.OrderReviewId;

import java.util.Optional;
import java.util.UUID;

public interface OrderReviewRepository {

    OrderReview save(OrderReview orderReview);

    Optional<OrderReview> findById(OrderReviewId reviewId);

    Optional<OrderReview> findByOrderId(UUID orderId);

    OrderReviewPageResult findActiveByRestaurantId(UUID restaurantId, int page, int size);

    OrderReviewPageResult findActiveByRestaurantIdAndStatus(UUID restaurantId, String status, int page, int size);

    OrderReviewPageResult findAllByRestaurantId(UUID restaurantId, int page, int size);

    OrderReviewPageResult findByRestaurantIdAndIsActive(UUID restaurantId, boolean isActive, int page, int size);

    RestaurantRatingCalculationResult calculateRatingAndCount(UUID restaurantId);

    com.berkay.order.service.domain.dto.review.RestaurantReviewStatistics getReviewStatistics(UUID restaurantId);
}
