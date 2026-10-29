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
                publicRestaurantController.getPublicRestaurants("test", java.util.List.of("turkish"), true, null, null, null, null, 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(restaurantApplicationService, times(1)).getPublicRestaurants(any(GetPublicRestaurantListQuery.class));
    }

    @Test
    void shouldReturnPublicRestaurantsWithRatingAndSortSuccessfully() {
        // Arrange
        GetPublicRestaurantListQueryResponse expectedResponse = new GetPublicRestaurantListQueryResponse(
                Collections.emptyList(), 0, 20, 0, 0, true);

        when(restaurantApplicationService.getPublicRestaurants(any(GetPublicRestaurantListQuery.class)))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetPublicRestaurantListQueryResponse> responseEntity = 
                publicRestaurantController.getPublicRestaurants("test", java.util.List.of("turkish"), true, 
                        java.math.BigDecimal.valueOf(100), 45, java.math.BigDecimal.valueOf(4.5), "MOST_REVIEWS", 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(restaurantApplicationService, times(1)).getPublicRestaurants(argThat(query -> 
                java.math.BigDecimal.valueOf(4.5).equals(query.getMinRating()) &&
                "MOST_REVIEWS".equals(query.getSortBy()) &&
                "test".equals(query.getSearchName())
        ));
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

    @Test
    void shouldReturnPublicProductsSuccessfully() {
        UUID restaurantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        com.berkay.restaurant.service.domain.dto.read.GetPublicProductQueryResponse productResponse =
                com.berkay.restaurant.service.domain.dto.read.GetPublicProductQueryResponse.builder()
                        .productId(productId)
                        .name("Burger")
                        .price(new java.math.BigDecimal("100.00"))
                        .inStock(true)
                        .available(false)
                        .build();

        com.berkay.restaurant.service.domain.dto.read.GetPublicProductListQueryResponse expectedResponse =
                new com.berkay.restaurant.service.domain.dto.read.GetPublicProductListQueryResponse(restaurantId, java.util.List.of(productResponse));

        when(restaurantApplicationService.getPublicProducts(eq(restaurantId)))
                .thenReturn(expectedResponse);

        ResponseEntity<com.berkay.restaurant.service.domain.dto.read.GetPublicProductListQueryResponse> responseEntity =
                publicRestaurantController.getPublicProducts(restaurantId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(restaurantApplicationService, times(1)).getPublicProducts(eq(restaurantId));
    }
}
