package com.berkay.restaurant.service.dataaccess.restaurant.mapper;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.valueobject.RestaurantName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RestaurantDataAccessMapperTest {

    private RestaurantDataAccessMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RestaurantDataAccessMapper();
    }

    @Test
    void shouldMapOpeningAndClosingTimeFromDomainToEntity() {
        UUID restaurantId = UUID.randomUUID();
        LocalTime opening = LocalTime.of(8, 30);
        LocalTime closing = LocalTime.of(23, 0);

        Restaurant domain = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .restaurantName(new RestaurantName("Test Resto"))
                .active(true)
                .available(true)
                .openingTime(opening)
                .closingTime(closing)
                .menu(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();

        RestaurantEntity entity = mapper.restaurantToRestaurantEntity(domain);

        assertNotNull(entity);
        assertEquals(restaurantId, entity.getRestaurantId());
        assertEquals("Test Resto", entity.getRestaurantName());
        assertEquals(opening, entity.getOpeningTime());
        assertEquals(closing, entity.getClosingTime());
    }

    @Test
    void shouldMapNullOpeningAndClosingTimeFromDomainToEntity() {
        UUID restaurantId = UUID.randomUUID();

        Restaurant domain = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .restaurantName(new RestaurantName("24/7 Resto"))
                .active(true)
                .available(true)
                .openingTime(null)
                .closingTime(null)
                .menu(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();

        RestaurantEntity entity = mapper.restaurantToRestaurantEntity(domain);

        assertNotNull(entity);
        assertNull(entity.getOpeningTime());
        assertNull(entity.getClosingTime());
    }

    @Test
    void shouldMapOpeningAndClosingTimeFromEntityToDomain() {
        UUID restaurantId = UUID.randomUUID();
        LocalTime opening = LocalTime.of(10, 0);
        LocalTime closing = LocalTime.of(22, 30);

        RestaurantEntity entity = RestaurantEntity.builder()
                .restaurantId(restaurantId)
                .restaurantName("Test Resto")
                .isActive(true)
                .available(true)
                .openingTime(opening)
                .closingTime(closing)
                .menu(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();

        Restaurant domain = mapper.restaurantEntityToRestaurant(entity);

        assertNotNull(domain);
        assertEquals(restaurantId, domain.getId().getValue());
        assertEquals(opening, domain.getOpeningTime());
        assertEquals(closing, domain.getClosingTime());
    }

    @Test
    void shouldMapOpeningAndClosingTimeToDomainWithoutMenu() {
        UUID restaurantId = UUID.randomUUID();
        LocalTime opening = LocalTime.of(11, 0);
        LocalTime closing = null;

        RestaurantEntity entity = RestaurantEntity.builder()
                .restaurantId(restaurantId)
                .restaurantName("Test Resto")
                .isActive(true)
                .available(true)
                .openingTime(opening)
                .closingTime(closing)
                .menu(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();

        Restaurant domain = mapper.restaurantEntityToRestaurantWithoutMenu(entity);

        assertNotNull(domain);
        assertEquals(restaurantId, domain.getId().getValue());
        assertEquals(opening, domain.getOpeningTime());
        assertNull(domain.getClosingTime());
    }
}
