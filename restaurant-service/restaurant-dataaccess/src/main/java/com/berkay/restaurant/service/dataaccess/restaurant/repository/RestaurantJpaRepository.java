package com.berkay.restaurant.service.dataaccess.restaurant.repository;

import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, UUID> {
    
    List<RestaurantEntity> findAllByRestaurantIdIn(List<UUID> restaurantIds);

    @Query("SELECT DISTINCT r FROM RestaurantEntity r LEFT JOIN r.cuisines c LEFT JOIN r.menu m WHERE r.isActive = true " +
           "AND (CAST(:name AS string) IS NULL OR LOWER(r.restaurantName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')) OR LOWER(m.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) " +
           "AND (:cuisineCodes IS NULL OR c.code IN :cuisineCodes) " +
           "AND (:available IS NULL OR r.available = :available) " +
           "AND (CAST(:maxMinimumOrderAmount AS bigdecimal) IS NULL OR r.minimumOrderAmount <= :maxMinimumOrderAmount) " +
           "AND (CAST(:maxDeliveryTime AS int) IS NULL OR r.averageDeliveryTimeInMinutes <= :maxDeliveryTime) " +
           "AND (CAST(:minRating AS bigdecimal) IS NULL OR r.averageRating >= :minRating)")
    Page<RestaurantEntity> findPublicRestaurants(@Param("name") String name, 
                                                 @Param("cuisineCodes") List<String> cuisineCodes, 
                                                 @Param("available") Boolean available, 
                                                 @Param("maxMinimumOrderAmount") java.math.BigDecimal maxMinimumOrderAmount, 
                                                 @Param("maxDeliveryTime") Integer maxDeliveryTime, 
                                                 @Param("minRating") java.math.BigDecimal minRating, 
                                                 Pageable pageable);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RestaurantEntity r WHERE r.restaurantId = :id")
    java.util.Optional<RestaurantEntity> findByIdWithLock(@Param("id") UUID id);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE RestaurantEntity r SET r.averageRating = :averageRating, r.totalReviews = :totalReviews, r.ratingUpdatedAt = :ratingUpdatedAt " +
           "WHERE r.restaurantId = :restaurantId AND (r.ratingUpdatedAt IS NULL OR r.ratingUpdatedAt <= :ratingUpdatedAt)")
    int updateRestaurantRating(@Param("restaurantId") UUID restaurantId,
                               @Param("averageRating") java.math.BigDecimal averageRating,
                               @Param("totalReviews") Integer totalReviews,
                               @Param("ratingUpdatedAt") java.time.ZonedDateTime ratingUpdatedAt);
}