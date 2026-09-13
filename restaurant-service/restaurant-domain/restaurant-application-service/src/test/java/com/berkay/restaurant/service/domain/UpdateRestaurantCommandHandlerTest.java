package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateRestaurantCommand;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.RestaurantInformationEvent;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import com.berkay.restaurant.service.domain.ports.output.repository.cuisine.CuisineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateRestaurantCommandHandlerTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantOutboxHelper restaurantOutboxHelper;

    @Mock
    private RestaurantDataMapper restaurantDataMapper;

    @Mock
    private CuisineRepository cuisineRepository;

    @InjectMocks
    private UpdateRestaurantCommandHandler updateRestaurantCommandHandler;

    private UUID restaurantId;
    private Restaurant restaurant;

    @BeforeEach
    public void setUp() {
        restaurantId = UUID.randomUUID();
        restaurant = mock(Restaurant.class);
    }

    @Test
    public void testUpdateRestaurant_Success_AllFields() {
        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurant.getId()).thenReturn(new RestaurantId(restaurantId));
        when(restaurantRepository.saveRestaurant(restaurant)).thenReturn(restaurant);

        RestaurantInformationEvent event = mock(RestaurantInformationEvent.class);
        when(restaurantDataMapper.restaurantToRestaurantInformationEvent(restaurant)).thenReturn(event);

        UpdateRestaurantCommand command = UpdateRestaurantCommand.builder()
                .restaurantId(restaurantId)
                .restaurantName("New Restaurant Name")
                .active(true)
                .available(true)
                .minimumOrderAmount(new BigDecimal("150.00"))
                .deliveryFee(new BigDecimal("25.00"))
                .city("Istanbul")
                .district("Kadikoy")
                .neighborhood("Moda")
                .street("Moda Cad.")
                .buildingNumber("10")
                .doorNumber("2")
                .phoneNumber("+905551234567")
                .averageDeliveryTimeInMinutes(30)
                .description("Best burgers")
                .logoUrl("https://logo.png")
                .bannerUrl("https://banner.png")
                .build();

        updateRestaurantCommandHandler.updateRestaurant(command);

        verify(restaurant).updateName("New Restaurant Name");
        verify(restaurant).updateActiveStatus(true);
        verify(restaurant).updateAvailability(true);
        verify(restaurant).updateMinimumOrderAmount(new Money(new BigDecimal("150.00")));
        verify(restaurant).updateDeliveryFee(new Money(new BigDecimal("25.00")));
        verify(restaurant).updateAddress(any());
        verify(restaurant).updatePhoneNumber("+905551234567");
        verify(restaurant).updateAverageDeliveryTime(30);
        verify(restaurant).updateDescription("Best burgers");
        verify(restaurant).updateLogoUrl("https://logo.png");
        verify(restaurant).updateBannerUrl("https://banner.png");
        verify(restaurantRepository).saveRestaurant(restaurant);
        verify(restaurantOutboxHelper).saveRestaurantOutboxMessage(eq(event), any(), isNull());
    }

    @Test
    public void testUpdateRestaurant_Success_PartialBannerAndAvailable() {
        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurant.getId()).thenReturn(new RestaurantId(restaurantId));
        when(restaurantRepository.saveRestaurant(restaurant)).thenReturn(restaurant);

        RestaurantInformationEvent event = mock(RestaurantInformationEvent.class);
        when(restaurantDataMapper.restaurantToRestaurantInformationEvent(restaurant)).thenReturn(event);

        UpdateRestaurantCommand command = UpdateRestaurantCommand.builder()
                .restaurantId(restaurantId)
                .available(false)
                .bannerUrl("https://newbanner.png")
                .build();

        updateRestaurantCommandHandler.updateRestaurant(command);

        verify(restaurant).updateAvailability(false);
        verify(restaurant).updateBannerUrl("https://newbanner.png");
        verify(restaurant).updateName(isNull());
        verify(restaurantRepository).saveRestaurant(restaurant);
    }

    @Test
    public void testUpdateRestaurant_NotFound() {
        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

        UpdateRestaurantCommand command = UpdateRestaurantCommand.builder()
                .restaurantId(restaurantId)
                .build();

        assertThrows(RestaurantNotFoundException.class, () -> updateRestaurantCommandHandler.updateRestaurant(command));
        verify(restaurantRepository, never()).saveRestaurant(any());
    }
}
