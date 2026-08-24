package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantPersonnelRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateRestaurantCommandHandlerTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantOutboxHelper restaurantOutboxHelper;

    @Mock
    private RestaurantPersonnelRepository restaurantPersonnelRepository;

    @Mock
    private com.berkay.restaurant.service.domain.ports.output.repository.cuisine.CuisineRepository cuisineRepository;

    private RestaurantDataMapper restaurantDataMapper = new RestaurantDataMapper();

    private RestaurantDomainService restaurantDomainService = new RestaurantDomainServiceImpl();

    private CreateRestaurantCommandHandler createRestaurantCommandHandler;

    @BeforeEach
    public void init() {
        createRestaurantCommandHandler = new CreateRestaurantCommandHandler(
                restaurantRepository,
                restaurantDataMapper,
                restaurantOutboxHelper,
                restaurantDomainService,
                restaurantPersonnelRepository,
                cuisineRepository
        );
    }

    @Test
    public void testCreateRestaurant_Success() {
        CreateRestaurantCommand createRestaurantCommand = CreateRestaurantCommand.builder()
                .restaurantName("Test Restaurant")
                .merchantId(UUID.randomUUID().toString())
                .active(true)
                .street("Test Street")
                .city("Test City")
                .postalCode("12345")
                .phoneNumber("5551234567")
                .minimumOrderAmount(new BigDecimal("10.00"))
                .deliveryFee(new BigDecimal("2.50"))
                .averageDeliveryTimeInMinutes(30)
                .cuisineIds(List.of())
                .description("Test Description")
                .logoUrl("http://test.com/logo.png")
                .products(List.of())
                .build();

        when(restaurantRepository.saveRestaurant(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant restaurant = invocation.getArgument(0);
            return restaurant;
        });

        CreateRestaurantResponse response = createRestaurantCommandHandler.createRestaurant(createRestaurantCommand);

        assertNotNull(response);
        assertNotNull(response.getRestaurantId());
    }

    @Test
    public void testCreateRestaurant_NegativeMinOrder_ThrowsException() {
        CreateRestaurantCommand createRestaurantCommand = CreateRestaurantCommand.builder()
                .restaurantName("Test Restaurant")
                .merchantId(UUID.randomUUID().toString())
                .active(true)
                .street("Test Street")
                .city("Test City")
                .postalCode("12345")
                .phoneNumber("5551234567")
                .minimumOrderAmount(new BigDecimal("-5.00"))
                .deliveryFee(new BigDecimal("2.50"))
                .averageDeliveryTimeInMinutes(30)
                .cuisineIds(List.of())
                .description("Test Description")
                .logoUrl("http://test.com/logo.png")
                .products(List.of())
                .build();

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class, () -> {
            createRestaurantCommandHandler.createRestaurant(createRestaurantCommand);
        });

        assertEquals("Minimum order amount must be greater than or equal to zero!", exception.getMessage());
    }

    @Test
    public void testCreateRestaurant_NegativeDeliveryFee_ThrowsException() {
        CreateRestaurantCommand createRestaurantCommand = CreateRestaurantCommand.builder()
                .restaurantName("Test Restaurant")
                .merchantId(UUID.randomUUID().toString())
                .active(true)
                .street("Test Street")
                .city("Test City")
                .postalCode("12345")
                .phoneNumber("5551234567")
                .minimumOrderAmount(new BigDecimal("10.00"))
                .deliveryFee(new BigDecimal("-2.50"))
                .averageDeliveryTimeInMinutes(30)
                .cuisineIds(List.of())
                .description("Test Description")
                .logoUrl("http://test.com/logo.png")
                .products(List.of())
                .build();

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class, () -> {
            createRestaurantCommandHandler.createRestaurant(createRestaurantCommand);
        });

        assertEquals("Delivery fee must be greater than or equal to zero!", exception.getMessage());
    }

    @Test
    public void testCreateRestaurant_NegativeDeliveryTime_ThrowsException() {
        CreateRestaurantCommand createRestaurantCommand = CreateRestaurantCommand.builder()
                .restaurantName("Test Restaurant")
                .merchantId(UUID.randomUUID().toString())
                .active(true)
                .street("Test Street")
                .city("Test City")
                .postalCode("12345")
                .phoneNumber("5551234567")
                .minimumOrderAmount(new BigDecimal("10.00"))
                .deliveryFee(new BigDecimal("2.50"))
                .averageDeliveryTimeInMinutes(-5)
                .cuisineIds(List.of())
                .description("Test Description")
                .logoUrl("http://test.com/logo.png")
                .products(List.of())
                .build();

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class, () -> {
            createRestaurantCommandHandler.createRestaurant(createRestaurantCommand);
        });

        assertEquals("Average delivery time cannot be negative!", exception.getMessage());
    }
}
