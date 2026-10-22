package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.ProductCategoryId;
import com.berkay.domain.valueobject.ProductId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.update.product.UpdateProductCommand;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.ProductCategory;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.ProductNotFoundException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateProductCommandHandlerTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantOutboxHelper restaurantOutboxHelper;

    private RestaurantDataMapper restaurantDataMapper = new RestaurantDataMapper();

    private UpdateProductCommandHandler updateProductCommandHandler;

    @BeforeEach
    public void init() {
        updateProductCommandHandler = new UpdateProductCommandHandler(
                restaurantRepository,
                restaurantOutboxHelper,
                restaurantDataMapper
        );
    }

    @Test
    public void testUpdateProduct_Success() {
        UUID restaurantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID oldCategoryId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();

        Product existingProduct = Product.builder()
                .productId(new ProductId(productId))
                .name("Old Pizza")
                .description("Old description")
                .price(new Money(new BigDecimal("10.00")))
                .stock(50)
                .available(true)
                .hidden(false)
                .imageUrl("https://old.com/img.jpg")
                .categoryId(new ProductCategoryId(oldCategoryId))
                .build();

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .categories(new ArrayList<>(List.of(
                        ProductCategory.builder()
                                .productCategoryId(new ProductCategoryId(oldCategoryId))
                                .name("Old Category")
                                .build(),
                        ProductCategory.builder()
                                .productCategoryId(new ProductCategoryId(newCategoryId))
                                .name("New Category")
                                .build()
                )))
                .menu(new ArrayList<>(List.of(existingProduct)))
                .build();

        UpdateProductCommand command = UpdateProductCommand.builder()
                .restaurantId(restaurantId)
                .productId(productId)
                .name("Updated Pizza")
                .description("Updated description")
                .price(new BigDecimal("25.00"))
                .stock(80)
                .available(false)
                .hidden(true)
                .imageUrl("https://new.com/img.jpg")
                .categoryId(newCategoryId)
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.saveRestaurant(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        updateProductCommandHandler.updateProduct(command);

        verify(restaurantRepository, times(1)).saveRestaurant(restaurant);
        verify(restaurantOutboxHelper, times(1)).saveRestaurantOutboxMessage(any(), any(), any());

        assertEquals("Updated Pizza", existingProduct.getName());
        assertEquals("Updated description", existingProduct.getDescription());
        assertEquals(new BigDecimal("25.00"), existingProduct.getPrice().getAmount());
        assertEquals(80, existingProduct.getStock());
        assertFalse(existingProduct.isAvailable());
        assertTrue(existingProduct.isHidden());
        assertEquals("https://new.com/img.jpg", existingProduct.getImageUrl());
        assertEquals(newCategoryId, existingProduct.getCategoryId().getValue());
    }

    @Test
    public void testUpdateProduct_ClearOptionalFields_Success() {
        UUID restaurantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Product existingProduct = Product.builder()
                .productId(new ProductId(productId))
                .name("Margherita")
                .description("Tasty pizza")
                .price(new Money(new BigDecimal("20.00")))
                .stock(10)
                .available(true)
                .hidden(false)
                .imageUrl("https://example.com/pizza.jpg")
                .categoryId(new ProductCategoryId(categoryId))
                .build();

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .categories(new ArrayList<>(List.of(
                        ProductCategory.builder()
                                .productCategoryId(new ProductCategoryId(categoryId))
                                .name("Pizzas")
                                .build()
                )))
                .menu(new ArrayList<>(List.of(existingProduct)))
                .build();

        // PUT request clearing description and imageUrl
        UpdateProductCommand command = UpdateProductCommand.builder()
                .restaurantId(restaurantId)
                .productId(productId)
                .name("Margherita")
                .description(null)
                .price(new BigDecimal("22.00"))
                .stock(15)
                .available(true)
                .hidden(false)
                .imageUrl(null)
                .categoryId(categoryId)
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.saveRestaurant(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        updateProductCommandHandler.updateProduct(command);

        assertNull(existingProduct.getDescription());
        assertNull(existingProduct.getImageUrl());
        assertEquals(new BigDecimal("22.00"), existingProduct.getPrice().getAmount());
        assertEquals(categoryId, existingProduct.getCategoryId().getValue());
    }

    @Test
    public void testUpdateProduct_RestaurantNotFound() {
        UUID restaurantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        UpdateProductCommand command = UpdateProductCommand.builder()
                .restaurantId(restaurantId)
                .productId(productId)
                .name("Pizza")
                .price(new BigDecimal("10.00"))
                .stock(5)
                .available(true)
                .hidden(false)
                .categoryId(UUID.randomUUID())
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> updateProductCommandHandler.updateProduct(command));
    }

    @Test
    public void testUpdateProduct_ProductNotFound() {
        UUID restaurantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .menu(new ArrayList<>())
                .build();

        UpdateProductCommand command = UpdateProductCommand.builder()
                .restaurantId(restaurantId)
                .productId(productId)
                .name("Pizza")
                .price(new BigDecimal("10.00"))
                .stock(5)
                .available(true)
                .hidden(false)
                .categoryId(UUID.randomUUID())
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        assertThrows(ProductNotFoundException.class, () -> updateProductCommandHandler.updateProduct(command));
    }
}
