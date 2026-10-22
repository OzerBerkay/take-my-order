package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQuery;
import com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQuery;
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
        GetPublicRestaurantListQuery query = new GetPublicRestaurantListQuery(null, List.of("fast_food"), true, null, null, 0, 10);
        RestaurantPageResult pageResult = new RestaurantPageResult(List.of(restaurant), 0, 10, 1, 1, true);

        when(restaurantRepository.findPublicRestaurants(null, List.of("fast_food"), true, null, null, 0, 10))
                .thenReturn(pageResult);

        GetPublicRestaurantListQueryResponse response = restaurantQueryHandler.getPublicRestaurants(query);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(1, response.getTotalElements());
        assertTrue(response.isLast());
    }

    @Test
    public void testGetRestaurant_Success() {
        when(restaurantRepository.findRestaurantByIdWithoutMenu(restaurantId)).thenReturn(Optional.of(restaurant));

        GetRestaurantQuery query = GetRestaurantQuery.builder()
                .restaurantId(restaurantId)
                .build();

        com.berkay.restaurant.service.domain.dto.read.GetRestaurantQueryResponse response = restaurantQueryHandler.getRestaurant(query);

        assertNotNull(response);
        assertEquals(restaurantId, response.getRestaurantId());
        assertEquals("Test Restaurant", response.getName());
        assertTrue(response.getActive());
    }

    @Test
    public void testGetRestaurant_NotFound() {
        when(restaurantRepository.findRestaurantByIdWithoutMenu(restaurantId)).thenReturn(Optional.empty());

        GetRestaurantQuery query = GetRestaurantQuery.builder()
                .restaurantId(restaurantId)
                .build();

        RestaurantNotFoundException exception = assertThrows(RestaurantNotFoundException.class, 
            () -> restaurantQueryHandler.getRestaurant(query));

        assertTrue(exception.getMessage().contains("Restaurant not found"));
    }

    @Test
    public void testGetPublicRestaurant_Success() {
        when(restaurantRepository.findRestaurantByIdWithoutMenu(restaurantId)).thenReturn(Optional.of(restaurant));

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

        when(restaurantRepository.findRestaurantByIdWithoutMenu(restaurantId)).thenReturn(Optional.of(inactiveRestaurant));

        RestaurantNotFoundException exception = assertThrows(RestaurantNotFoundException.class, 
            () -> restaurantQueryHandler.getPublicRestaurant(restaurantId));

        assertTrue(exception.getMessage().contains("Active restaurant not found"));
    }

    @Test
    public void testGetPublicRestaurant_NotFound() {
        when(restaurantRepository.findRestaurantByIdWithoutMenu(restaurantId)).thenReturn(Optional.empty());

        RestaurantNotFoundException exception = assertThrows(RestaurantNotFoundException.class, 
            () -> restaurantQueryHandler.getPublicRestaurant(restaurantId));

        assertTrue(exception.getMessage().contains("Restaurant not found"));
    }

    @Test
    public void testGetPublicProducts_WithAvailableStatus_Success() {
        UUID availableProductId = UUID.randomUUID();
        UUID unavailableProductId = UUID.randomUUID();
        UUID hiddenProductId = UUID.randomUUID();

        com.berkay.restaurant.service.domain.entity.Product availableProduct = com.berkay.restaurant.service.domain.entity.Product.builder()
                .productId(new com.berkay.domain.valueobject.ProductId(availableProductId))
                .name("Available Burger")
                .price(new com.berkay.domain.valueobject.Money(new java.math.BigDecimal("100.00")))
                .stock(10)
                .available(true)
                .hidden(false)
                .build();

        com.berkay.restaurant.service.domain.entity.Product unavailableProduct = com.berkay.restaurant.service.domain.entity.Product.builder()
                .productId(new com.berkay.domain.valueobject.ProductId(unavailableProductId))
                .name("Unavailable Burger")
                .price(new com.berkay.domain.valueobject.Money(new java.math.BigDecimal("120.00")))
                .stock(5)
                .available(false)
                .hidden(false)
                .build();

        com.berkay.restaurant.service.domain.entity.Product hiddenProduct = com.berkay.restaurant.service.domain.entity.Product.builder()
                .productId(new com.berkay.domain.valueobject.ProductId(hiddenProductId))
                .name("Secret Burger")
                .price(new com.berkay.domain.valueobject.Money(new java.math.BigDecimal("150.00")))
                .stock(20)
                .available(true)
                .hidden(true)
                .build();

        Restaurant restaurantWithMenu = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .restaurantName(new com.berkay.restaurant.service.domain.valueobject.RestaurantName("Burger House"))
                .active(true)
                .menu(List.of(availableProduct, unavailableProduct, hiddenProduct))
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurantWithMenu));

        com.berkay.restaurant.service.domain.dto.read.GetPublicProductListQueryResponse response =
                restaurantQueryHandler.getPublicProducts(restaurantId);

        assertNotNull(response);
        assertEquals(2, response.getProducts().size());

        com.berkay.restaurant.service.domain.dto.read.GetPublicProductQueryResponse first = response.getProducts().get(0);
        assertEquals("Available Burger", first.getName());
        assertTrue(first.getAvailable());
        assertTrue(first.getInStock());

        com.berkay.restaurant.service.domain.dto.read.GetPublicProductQueryResponse second = response.getProducts().get(1);
        assertEquals("Unavailable Burger", second.getName());
        assertFalse(second.getAvailable());
        assertTrue(second.getInStock());
    }

    @Test
    public void testGetPublicProduct_Success() {
        UUID prodId = UUID.randomUUID();

        com.berkay.restaurant.service.domain.entity.Product product = com.berkay.restaurant.service.domain.entity.Product.builder()
                .productId(new com.berkay.domain.valueobject.ProductId(prodId))
                .name("Special Pizza")
                .price(new com.berkay.domain.valueobject.Money(new java.math.BigDecimal("85.00")))
                .stock(15)
                .available(false)
                .hidden(false)
                .build();

        Restaurant restaurantWithMenu = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .restaurantName(new com.berkay.restaurant.service.domain.valueobject.RestaurantName("Pizza Hub"))
                .active(true)
                .menu(List.of(product))
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurantWithMenu));

        com.berkay.restaurant.service.domain.dto.read.GetPublicProductQueryResponse response =
                restaurantQueryHandler.getPublicProduct(restaurantId, prodId);

        assertNotNull(response);
        assertEquals(prodId, response.getProductId());
        assertEquals("Special Pizza", response.getName());
        assertFalse(response.getAvailable());
        assertTrue(response.getInStock());
    }
}
