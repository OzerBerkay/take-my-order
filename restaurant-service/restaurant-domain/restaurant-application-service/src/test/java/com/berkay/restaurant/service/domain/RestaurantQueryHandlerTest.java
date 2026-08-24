package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQuery;
import com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.RestaurantPageResult;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.entity.RestaurantPersonnel;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantPersonnelRepository;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestaurantQueryHandlerTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantPersonnelRepository restaurantPersonnelRepository;

    private RestaurantDataMapper restaurantDataMapper;

    private RestaurantQueryHandler restaurantQueryHandler;

    private UUID restaurantId;
    private UUID userId;
    private Restaurant restaurant;

    @BeforeEach
    public void setUp() {
        restaurantDataMapper = new RestaurantDataMapper();
        restaurantQueryHandler = new RestaurantQueryHandler(restaurantRepository, restaurantPersonnelRepository, restaurantDataMapper);

        restaurantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        
        restaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .restaurantName(new com.berkay.restaurant.service.domain.valueobject.RestaurantName("Test Restaurant"))
                .active(true)
                .build();
    }

    @Test
    public void testGetRestaurants_Success() {
        RestaurantPersonnel personnel = RestaurantPersonnel.builder()
                .restaurantPersonnelId(new com.berkay.restaurant.service.domain.valueobject.RestaurantPersonnelId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(restaurantId))
                .userId(userId)
                .build();

        when(restaurantPersonnelRepository.findByUserId(userId)).thenReturn(List.of(personnel));
        when(restaurantRepository.findAllByIdIn(List.of(restaurantId))).thenReturn(List.of(restaurant));

        GetRestaurantListQueryResponse response = restaurantQueryHandler.getRestaurants(userId);

        assertNotNull(response);
        assertEquals(1, response.getRestaurants().size());
        assertEquals("Test Restaurant", response.getRestaurants().get(0).getName());
    }

    @Test
    public void testGetRestaurants_NoPersonnelFound() {
        when(restaurantPersonnelRepository.findByUserId(userId)).thenReturn(List.of());

        GetRestaurantListQueryResponse response = restaurantQueryHandler.getRestaurants(userId);

        assertNotNull(response);
        assertTrue(response.getRestaurants().isEmpty());
    }

    @Test
    public void testGetPublicRestaurants_Success() {
        GetPublicRestaurantListQuery query = new GetPublicRestaurantListQuery(null, List.of("fast_food"), true, 0, 10);
        RestaurantPageResult pageResult = new RestaurantPageResult(List.of(restaurant), 0, 10, 1, 1, true);

        when(restaurantRepository.findPublicRestaurants(null, List.of("fast_food"), true, 0, 10))
                .thenReturn(pageResult);

        GetPublicRestaurantListQueryResponse response = restaurantQueryHandler.getPublicRestaurants(query);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(1, response.getTotalElements());
        assertTrue(response.isLast());
    }

    @Test
    public void testGetPublicRestaurant_Success() {
        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        GetPublicRestaurantQueryResponse response = restaurantQueryHandler.getPublicRestaurant(restaurantId);

        assertNotNull(response);
        assertEquals(restaurantId, response.getRestaurant().getRestaurantId());
        assertEquals("Test Restaurant", response.getRestaurant().getName());
    }

    @Test
    public void testGetPublicRestaurant_NotActive() {
        Restaurant inactiveRestaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .restaurantName(new com.berkay.restaurant.service.domain.valueobject.RestaurantName("Inactive Restaurant"))
                .active(false)
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(inactiveRestaurant));

        RestaurantNotFoundException exception = assertThrows(RestaurantNotFoundException.class, 
            () -> restaurantQueryHandler.getPublicRestaurant(restaurantId));

        assertTrue(exception.getMessage().contains("Active restaurant not found"));
    }

    @Test
    public void testGetPublicRestaurant_NotFound() {
        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

        RestaurantNotFoundException exception = assertThrows(RestaurantNotFoundException.class, 
            () -> restaurantQueryHandler.getPublicRestaurant(restaurantId));

        assertTrue(exception.getMessage().contains("Restaurant not found"));
    }
}
