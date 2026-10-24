package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductBatchCommand;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductBatchResponse;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateProductCommand;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxEventType;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddProductBatchCommandHandlerTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantOutboxHelper restaurantOutboxHelper;

    private RestaurantDataMapper restaurantDataMapper = new RestaurantDataMapper();

    private AddProductBatchCommandHandler addProductBatchCommandHandler;

    @BeforeEach
    public void init() {
        addProductBatchCommandHandler = new AddProductBatchCommandHandler(
                restaurantRepository,
                restaurantDataMapper,
                restaurantOutboxHelper
        );
    }

    @Test
    public void testAddProductBatch_Success() {
        UUID restaurantId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        CreateProductCommand product1 = CreateProductCommand.builder()
                .name("Burger 1")
                .price(new BigDecimal("120.00"))
                .stock(50)
                .available(true)
                .hidden(false)
                .categoryId(categoryId)
                .build();

        CreateProductCommand product2 = CreateProductCommand.builder()
                .name("Burger 2")
                .price(new BigDecimal("140.00"))
                .stock(30)
                .available(true)
                .hidden(false)
                .categoryId(categoryId)
                .build();

        AddProductBatchCommand command = AddProductBatchCommand.builder()
                .restaurantId(restaurantId)
                .products(List.of(product1, product2))
                .build();

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .categories(new ArrayList<>(List.of(com.berkay.restaurant.service.domain.entity.ProductCategory.builder()
                        .productCategoryId(new com.berkay.domain.valueobject.ProductCategoryId(categoryId))
                        .name("Burgers")
                        .sortOrder(1)
                        .build())))
                .menu(new ArrayList<>())
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.saveRestaurant(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddProductBatchResponse response = addProductBatchCommandHandler.addProductBatch(command);

        assertNotNull(response);
        assertEquals("Products added successfully", response.getMessage());
        assertEquals(2, response.getProductIds().size());

        verify(restaurantRepository, times(1)).saveRestaurant(restaurant);
        verify(restaurantOutboxHelper, times(1)).saveRestaurantOutboxMessage(
                any(),
                eq(RestaurantOutboxEventType.RESTAURANT_UPDATED),
                isNull()
        );

        assertEquals(2, restaurant.getMenu().size());
        assertEquals("Burger 1", restaurant.getMenu().get(0).getName());
        assertEquals("Burger 2", restaurant.getMenu().get(1).getName());
    }

    @Test
    public void testAddProductBatch_RestaurantNotFound() {
        UUID restaurantId = UUID.randomUUID();
        AddProductBatchCommand command = AddProductBatchCommand.builder()
                .restaurantId(restaurantId)
                .products(List.of())
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> addProductBatchCommandHandler.addProductBatch(command));

        verify(restaurantRepository, never()).saveRestaurant(any());
        verify(restaurantOutboxHelper, never()).saveRestaurantOutboxMessage(any(), any(), any());
    }
}
