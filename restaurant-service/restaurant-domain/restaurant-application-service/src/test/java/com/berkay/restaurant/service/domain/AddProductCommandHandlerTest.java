package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.ProductId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductResponse;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddProductCommandHandlerTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantOutboxHelper restaurantOutboxHelper;

    private RestaurantDataMapper restaurantDataMapper = new RestaurantDataMapper();

    private AddProductCommandHandler addProductCommandHandler;

    @BeforeEach
    public void init() {
        addProductCommandHandler = new AddProductCommandHandler(
                restaurantRepository,
                restaurantDataMapper,
                restaurantOutboxHelper
        );
    }

    @Test
    public void testAddProduct_Success() {
        UUID restaurantId = UUID.randomUUID();
        AddProductCommand command = AddProductCommand.builder()
                .restaurantId(restaurantId)
                .name("New Pizza")
                .price(new BigDecimal("15.50"))
                .stock(100)
                .available(true)
                .build();

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .menu(new ArrayList<>())
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.saveRestaurant(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddProductResponse response = addProductCommandHandler.addProduct(command);

        assertNotNull(response);
        assertEquals("Product added successfully", response.getMessage());
        
        verify(restaurantRepository, times(1)).saveRestaurant(restaurant);
        verify(restaurantOutboxHelper, times(1)).saveRestaurantOutboxMessage(any(), any(), any());
        
        assertEquals(1, restaurant.getMenu().size());
        assertEquals("New Pizza", restaurant.getMenu().get(0).getName());
    }

    @Test
    public void testAddProduct_RestaurantNotFound() {
        UUID restaurantId = UUID.randomUUID();
        AddProductCommand command = AddProductCommand.builder()
                .restaurantId(restaurantId)
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> addProductCommandHandler.addProduct(command));
        
        verify(restaurantRepository, never()).saveRestaurant(any());
        verify(restaurantOutboxHelper, never()).saveRestaurantOutboxMessage(any(), any(), any());
    }
}
