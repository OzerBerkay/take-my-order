package com.berkay.order.service.dataaccess.review.adapter;

import com.berkay.order.service.dataaccess.review.entity.OrderReviewEntity;
import com.berkay.order.service.dataaccess.review.mapper.OrderReviewDataAccessMapper;
import com.berkay.order.service.dataaccess.review.repository.OrderReviewJpaRepository;
import com.berkay.order.service.domain.dto.restaurant.RestaurantRatingCalculationResult;
import com.berkay.order.service.domain.dto.review.OrderReviewPageResult;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import com.berkay.order.service.domain.valueobject.OrderReviewId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderReviewRepositoryImpl implements OrderReviewRepository {

    private final OrderReviewJpaRepository repository;
    private final OrderReviewDataAccessMapper mapper;

    public OrderReviewRepositoryImpl(OrderReviewJpaRepository repository,
                                     OrderReviewDataAccessMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public OrderReview save(OrderReview orderReview) {
        OrderReviewEntity entity = mapper.orderReviewToOrderReviewEntity(orderReview);
        return mapper.orderReviewEntityToOrderReview(repository.save(entity));
    }

    @Override
    public Optional<OrderReview> findById(OrderReviewId reviewId) {
        return repository.findById(reviewId.getValue())
                .map(mapper::orderReviewEntityToOrderReview);
    }

    @Override
    public Optional<OrderReview> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId)
                .map(mapper::orderReviewEntityToOrderReview);
    }

    @Override
    public OrderReviewPageResult findActiveByRestaurantId(UUID restaurantId, int page, int size) {
        return findActiveByRestaurantIdAndStatus(restaurantId, "ALL", page, size);
    }

    @Override
    public OrderReviewPageResult findActiveByRestaurantIdAndStatus(UUID restaurantId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String normalized = status != null ? status.trim().toUpperCase() : "ALL";
        Page<OrderReviewEntity> pageEntity;
        if ("REPLIED".equals(normalized)) {
            pageEntity = repository.findByRestaurantIdAndIsActiveTrueAndRestaurantReplyIsNotNull(restaurantId, pageable);
        } else if ("PENDING".equals(normalized) || "AWAITING_REPLY".equals(normalized)) {
            java.time.ZonedDateTime cutoffTime = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC")).minusDays(7);
            pageEntity = repository.findByRestaurantIdAndIsActiveTrueAndRestaurantReplyIsNullAndCreatedAtGreaterThanEqual(
                    restaurantId, cutoffTime, pageable);
        } else {
            pageEntity = repository.findByRestaurantIdAndIsActiveTrue(restaurantId, pageable);
        }
        return toPageResult(pageEntity);
    }

    @Override
    public OrderReviewPageResult findAllByRestaurantId(UUID restaurantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderReviewEntity> pageEntity = repository.findByRestaurantId(restaurantId, pageable);
        return toPageResult(pageEntity);
    }

    @Override
    public OrderReviewPageResult findByRestaurantIdAndIsActive(UUID restaurantId, boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderReviewEntity> pageEntity = repository.findByRestaurantIdAndIsActive(restaurantId, isActive, pageable);
        return toPageResult(pageEntity);
    }

    @Override
    public RestaurantRatingCalculationResult calculateRatingAndCount(UUID restaurantId) {
        List<Object[]> rows = repository.calculateRatingAndCount(restaurantId);
        if (rows != null && !rows.isEmpty() && rows.get(0) != null) {
            Object[] row = rows.get(0);
            long totalReviews = row[0] != null ? ((Number) row[0]).longValue() : 0L;
            Double avgRating = null;
            if (row[1] != null) {
                double rawAvg = ((Number) row[1]).doubleValue();
                avgRating = java.math.BigDecimal.valueOf(rawAvg).setScale(1, java.math.RoundingMode.HALF_UP).doubleValue();
            }
            return new RestaurantRatingCalculationResult(totalReviews, avgRating);
        }
        return new RestaurantRatingCalculationResult(0L, null);
    }

    @Override
    public com.berkay.order.service.domain.dto.review.RestaurantReviewStatistics getReviewStatistics(UUID restaurantId) {
        java.time.ZonedDateTime cutoffTime = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC")).minusDays(7);
        List<Object[]> rows = repository.getReviewStatistics(restaurantId, cutoffTime);
        if (rows != null && !rows.isEmpty() && rows.get(0) != null) {
            Object[] row = rows.get(0);
            long totalCount = row[0] != null ? ((Number) row[0]).longValue() : 0L;
            long repliedCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            long pendingReplyCount = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            return new com.berkay.order.service.domain.dto.review.RestaurantReviewStatistics(totalCount, repliedCount, pendingReplyCount);
        }
        return new com.berkay.order.service.domain.dto.review.RestaurantReviewStatistics(0L, 0L, 0L);
    }

    private OrderReviewPageResult toPageResult(Page<OrderReviewEntity> pageEntity) {
        List<OrderReview> reviews = pageEntity.getContent().stream()
                .map(mapper::orderReviewEntityToOrderReview)
                .collect(Collectors.toList());
        return OrderReviewPageResult.builder()
                .reviews(reviews)
                .page(pageEntity.getNumber())
                .size(pageEntity.getSize())
                .totalElements(pageEntity.getTotalElements())
                .totalPages(pageEntity.getTotalPages())
                .isLast(pageEntity.isLast())
                .build();
    }
}
