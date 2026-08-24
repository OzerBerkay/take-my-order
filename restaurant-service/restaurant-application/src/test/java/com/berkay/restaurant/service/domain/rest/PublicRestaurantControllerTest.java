package com.berkay.restaurant.service.domain.rest;

import com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQuery;
import com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantQueryResponse;
import com.berkay.restaurant.service.domain.ports.input.service.RestaurantApplicationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PublicRestaurantControllerTest {

    @Mock
    private RestaurantApplicationService restaurantApplicationService;

    @InjectMocks
    private PublicRestaurantController publicRestaurantController;

    @Test
    void shouldReturnPublicRestaurantsSuccessfully() {
        // Arrange
        GetPublicRestaurantListQueryResponse expectedResponse = new GetPublicRestaurantListQueryResponse(
                Collections.emptyList(), 0, 20, 0, 0, true);

        when(restaurantApplicationService.getPublicRestaurants(any(GetPublicRestaurantListQuery.class)))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetPublicRestaurantListQueryResponse> responseEntity = 
                publicRestaurantController.getPublicRestaurants("test", java.util.List.of("turkish"), true, 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(restaurantApplicationService, times(1)).getPublicRestaurants(any(GetPublicRestaurantListQuery.class));
    }

    @Test
    void shouldReturnSinglePublicRestaurantSuccessfully() {
        // Arrange
        UUID restaurantId = UUID.randomUUID();
        GetPublicRestaurantQueryResponse expectedResponse = new GetPublicRestaurantQueryResponse(null);

        when(restaurantApplicationService.getPublicRestaurant(eq(restaurantId)))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetPublicRestaurantQueryResponse> responseEntity = 
                publicRestaurantController.getPublicRestaurant(restaurantId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(restaurantApplicationService, times(1)).getPublicRestaurant(eq(restaurantId));
    }
}
