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
        Page<RestaurantEntity> result = restaurantJpaRepository.findPublicRestaurants(null, null, null, null, null, PageRequest.of(0, 10));

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
        Page<RestaurantEntity> result = restaurantJpaRepository.findPublicRestaurants("kebab", null, null, null, null, PageRequest.of(0, 10));

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
        Page<RestaurantEntity> result = restaurantJpaRepository.findPublicRestaurants(null, null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Open Restaurant", result.getContent().get(0).getRestaurantName());
        assertEquals("Closed Restaurant", result.getContent().get(1).getRestaurantName());
    }
}
