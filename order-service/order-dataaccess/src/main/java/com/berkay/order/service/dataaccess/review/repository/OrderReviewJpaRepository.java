package com.berkay.order.service.dataaccess.review.repository;

import com.berkay.order.service.dataaccess.review.entity.OrderReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderReviewJpaRepository extends JpaRepository<OrderReviewEntity, UUID> {

    Optional<OrderReviewEntity> findByOrderId(UUID orderId);

    Page<OrderReviewEntity> findByRestaurantIdAndIsActiveTrue(UUID restaurantId, Pageable pageable);

    Page<OrderReviewEntity> findByRestaurantId(UUID restaurantId, Pageable pageable);

    Page<OrderReviewEntity> findByRestaurantIdAndIsActive(UUID restaurantId, boolean isActive, Pageable pageable);

    Page<OrderReviewEntity> findByRestaurantIdAndIsActiveTrueAndRestaurantReplyIsNotNull(UUID restaurantId, Pageable pageable);

    Page<OrderReviewEntity> findByRestaurantIdAndIsActiveTrueAndRestaurantReplyIsNullAndCreatedAtGreaterThanEqual(UUID restaurantId, java.time.ZonedDateTime cutoffTime, Pageable pageable);

    @Query("SELECT COUNT(r.id), AVG(r.rating) FROM OrderReviewEntity r WHERE r.restaurantId = :restaurantId AND r.isActive = true")
    List<Object[]> calculateRatingAndCount(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT " +
            "COUNT(r.id), " +
            "COUNT(CASE WHEN r.restaurantReply IS NOT NULL THEN 1 END), " +
            "COUNT(CASE WHEN r.restaurantReply IS NULL AND r.createdAt >= :cutoffTime THEN 1 END) " +
            "FROM OrderReviewEntity r " +
            "WHERE r.restaurantId = :restaurantId AND r.isActive = true")
    List<Object[]> getReviewStatistics(@Param("restaurantId") UUID restaurantId, @Param("cutoffTime") java.time.ZonedDateTime cutoffTime);
}
