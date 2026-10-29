package com.berkay.restaurant.service.dataaccess.restaurant.repository;

import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantEntity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = RestaurantJpaRepositoryTest.TestConfig.class)
public class RestaurantJpaRepositoryTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.berkay.restaurant.service.dataaccess")
    @EnableJpaRepositories(basePackages = "com.berkay.restaurant.service.dataaccess")
    static class TestConfig {
    }

    @Autowired
    private RestaurantJpaRepository restaurantJpaRepository;

    @Test
    void shouldFindPublicRestaurantsWithNullParametersWithoutTypeException() {
        // Arrange
        RestaurantEntity entity = RestaurantEntity.builder()
                .restaurantId(UUID.randomUUID())
                .restaurantName("Test Restaurant")
                .isActive(true)
                .cuisines(new java.util.HashSet<>())
                .averageDeliveryTimeInMinutes(30)
                .deliveryFee(BigDecimal.TEN)
                .minimumOrderAmount(BigDecimal.valueOf(50))
                .build();
                
        restaurantJpaRepository.save(entity);

        // Act - Testing with null parameters to ensure PostgreSQL type inference (bytea) does not throw exception
        org.springframework.data.domain.Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("available"),
                org.springframework.data.domain.Sort.Order.asc("restaurantName")));
        Page<RestaurantEntity> result = restaurantJpaRepository.findPublicRestaurants(null, null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Restaurant", result.getContent().get(0).getRestaurantName());
    }

    @Test
    void shouldFindPublicRestaurantsWithSearchName() {
        // Arrange
        RestaurantEntity entity = RestaurantEntity.builder()
                .restaurantId(UUID.randomUUID())
                .restaurantName("Kebab House")
                .isActive(true)
                .cuisines(new java.util.HashSet<>())
                .build();
                
        restaurantJpaRepository.save(entity);

        // Act
        org.springframework.data.domain.Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("available"),
                org.springframework.data.domain.Sort.Order.asc("restaurantName")));
        Page<RestaurantEntity> result = restaurantJpaRepository.findPublicRestaurants("kebab", null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Kebab House", result.getContent().get(0).getRestaurantName());
    }

    @Test
    void shouldSortAvailableRestaurantsFirst() {
        // Arrange
        RestaurantEntity closedEntity = RestaurantEntity.builder()
                .restaurantId(UUID.randomUUID())
                .restaurantName("Closed Restaurant")
                .isActive(true)
                .available(false)
                .build();

        RestaurantEntity openEntity = RestaurantEntity.builder()
                .restaurantId(UUID.randomUUID())
                .restaurantName("Open Restaurant")
                .isActive(true)
                .available(true)
                .build();

        restaurantJpaRepository.save(closedEntity);
        restaurantJpaRepository.save(openEntity);

        // Act
        org.springframework.data.domain.Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("available"),
                org.springframework.data.domain.Sort.Order.asc("restaurantName")));
        Page<RestaurantEntity> result = restaurantJpaRepository.findPublicRestaurants(null, null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Open Restaurant", result.getContent().get(0).getRestaurantName());
        assertEquals("Closed Restaurant", result.getContent().get(1).getRestaurantName());
    }

    @Test
    void shouldFilterPublicRestaurantsByMinRating() {
        // Arrange
        RestaurantEntity rLow = RestaurantEntity.builder()
                .restaurantId(UUID.randomUUID())
                .restaurantName("Low Rating Cafe")
                .isActive(true)
                .averageRating(BigDecimal.valueOf(3.2))
                .build();

        RestaurantEntity rMid = RestaurantEntity.builder()
                .restaurantId(UUID.randomUUID())
                .restaurantName("Mid Rating Bistro")
                .isActive(true)
                .averageRating(BigDecimal.valueOf(4.2))
                .build();

        RestaurantEntity rHigh = RestaurantEntity.builder()
                .restaurantId(UUID.randomUUID())
                .restaurantName("High Rating Fine Dining")
                .isActive(true)
                .averageRating(BigDecimal.valueOf(4.8))
                .build();

        restaurantJpaRepository.save(rLow);
        restaurantJpaRepository.save(rMid);
        restaurantJpaRepository.save(rHigh);

        org.springframework.data.domain.Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("available"),
                org.springframework.data.domain.Sort.Order.asc("restaurantName")));

        // Act & Assert 1: minRating = 4.0 (should return 4.2 and 4.8)
        Page<RestaurantEntity> result40 = restaurantJpaRepository.findPublicRestaurants(
                null, null, null, null, null, BigDecimal.valueOf(4.0), pageable);
        assertEquals(2, result40.getTotalElements());

        // Act & Assert 2: minRating = 4.5 (should return 4.8 only)
        Page<RestaurantEntity> result45 = restaurantJpaRepository.findPublicRestaurants(
                null, null, null, null, null, BigDecimal.valueOf(4.5), pageable);
        assertEquals(1, result45.getTotalElements());
        assertEquals("High Rating Fine Dining", result45.getContent().get(0).getRestaurantName());
    }

    @Test
    void shouldSortByReviewsRatingFeeAndDeliveryTime() {
        // Arrange
        RestaurantEntity r1 = RestaurantEntity.builder()
                .restaurantId(UUID.randomUUID())
                .restaurantName("Alpha Diner")
                .isActive(true)
                .available(true)
                .totalReviews(5)
                .averageRating(BigDecimal.valueOf(3.8))
                .deliveryFee(BigDecimal.valueOf(30))
                .averageDeliveryTimeInMinutes(45)
                .build();

        RestaurantEntity r2 = RestaurantEntity.builder()
                .restaurantId(UUID.randomUUID())
                .restaurantName("Beta Diner")
                .isActive(true)
                .available(true)
                .totalReviews(50)
                .averageRating(BigDecimal.valueOf(4.9))
                .deliveryFee(BigDecimal.valueOf(10))
                .averageDeliveryTimeInMinutes(20)
                .build();

        restaurantJpaRepository.save(r1);
        restaurantJpaRepository.save(r2);

        // Sort by totalReviews DESC (Beta: 50 first, Alpha: 5 second)
        org.springframework.data.domain.Pageable sortReviews = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("available"),
                org.springframework.data.domain.Sort.Order.desc("totalReviews").nullsLast(),
                org.springframework.data.domain.Sort.Order.asc("restaurantName")));
        Page<RestaurantEntity> resReviews = restaurantJpaRepository.findPublicRestaurants(null, null, null, null, null, null, sortReviews);
        assertEquals("Beta Diner", resReviews.getContent().get(0).getRestaurantName());

        // Sort by averageRating DESC (Beta: 4.9 first, Alpha: 3.8 second)
        org.springframework.data.domain.Pageable sortRating = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("available"),
                org.springframework.data.domain.Sort.Order.desc("averageRating").nullsLast(),
                org.springframework.data.domain.Sort.Order.asc("restaurantName")));
        Page<RestaurantEntity> resRating = restaurantJpaRepository.findPublicRestaurants(null, null, null, null, null, null, sortRating);
        assertEquals("Beta Diner", resRating.getContent().get(0).getRestaurantName());

        // Sort by deliveryFee ASC (Beta: 10 first, Alpha: 30 second)
        org.springframework.data.domain.Pageable sortFee = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("available"),
                org.springframework.data.domain.Sort.Order.asc("deliveryFee").nullsLast(),
                org.springframework.data.domain.Sort.Order.asc("restaurantName")));
        Page<RestaurantEntity> resFee = restaurantJpaRepository.findPublicRestaurants(null, null, null, null, null, null, sortFee);
        assertEquals("Beta Diner", resFee.getContent().get(0).getRestaurantName());

        // Sort by averageDeliveryTimeInMinutes ASC (Beta: 20 first, Alpha: 45 second)
        org.springframework.data.domain.Pageable sortTime = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("available"),
                org.springframework.data.domain.Sort.Order.asc("averageDeliveryTimeInMinutes").nullsLast(),
                org.springframework.data.domain.Sort.Order.asc("restaurantName")));
        Page<RestaurantEntity> resTime = restaurantJpaRepository.findPublicRestaurants(null, null, null, null, null, null, sortTime);
        assertEquals("Beta Diner", resTime.getContent().get(0).getRestaurantName());
    }
}
