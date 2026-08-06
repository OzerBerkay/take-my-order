package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.ProductId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.delete.DeleteProductCommand;
import com.berkay.restaurant.service.domain.entity.Product;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteProductCommandHandlerTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantOutboxHelper restaurantOutboxHelper;

    private RestaurantDataMapper restaurantDataMapper = new RestaurantDataMapper();

    private DeleteProductCommandHandler deleteProductCommandHandler;

    @BeforeEach
    public void init() {
        deleteProductCommandHandler = new DeleteProductCommandHandler(
                restaurantRepository,
                restaurantOutboxHelper,
                restaurantDataMapper
        );
    }

    @Test
    public void testDeleteProduct_Success() {
        UUID restaurantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        
        DeleteProductCommand command = DeleteProductCommand.builder()
                .restaurantId(restaurantId)
                .productId(productId)
                .build();

        Product product = Product.builder()
                .productId(new ProductId(productId))
                .name("Pizza")
                .build();
                
        List<Product> products = new ArrayList<>();
        products.add(product);

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .menu(products)
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.saveRestaurant(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        deleteProductCommandHandler.deleteProduct(command);

        verify(restaurantRepository, times(1)).saveRestaurant(restaurant);
        verify(restaurantOutboxHelper, times(1)).saveRestaurantOutboxMessage(any(), any(), any());
        
        assertEquals(0, restaurant.getMenu().size());
    }

    @Test
    public void testDeleteProduct_RestaurantNotFound() {
        UUID restaurantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        
        DeleteProductCommand command = DeleteProductCommand.builder()
                .restaurantId(restaurantId)
                .productId(productId)
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> deleteProductCommandHandler.deleteProduct(command));
        
        verify(restaurantRepository, never()).saveRestaurant(any());
        verify(restaurantOutboxHelper, never()).saveRestaurantOutboxMessage(any(), any(), any());
    }
    
    @Test
    public void testDeleteProduct_ProductNotFound() {
        UUID restaurantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        
        DeleteProductCommand command = DeleteProductCommand.builder()
                .restaurantId(restaurantId)
                .productId(productId)
                .build();
                
        List<Product> products = new ArrayList<>();

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .menu(products) // Empty menu
                .build();

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        assertThrows(ProductNotFoundException.class, () -> deleteProductCommandHandler.deleteProduct(command));
        
        verify(restaurantRepository, never()).saveRestaurant(any());
        verify(restaurantOutboxHelper, never()).saveRestaurantOutboxMessage(any(), any(), any());
    }
}
