package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.update.restaurant.CategoryPayload;
import com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateCategoriesCommand;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateCategoriesCommandHandlerTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private UpdateCategoriesCommandHandler updateCategoriesCommandHandler;

    private UUID restaurantId;
    private Restaurant restaurant;

    @BeforeEach
    public void setUp() {
        restaurantId = UUID.randomUUID();
        restaurant = mock(Restaurant.class);
    }

    @Test
    public void testUpdateCategories_Success() {
        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        CategoryPayload payload = CategoryPayload.builder()
                .id(UUID.randomUUID())
                .name("Category 1")
                .sortOrder(1)
                .build();

        UpdateCategoriesCommand command = UpdateCategoriesCommand.builder()
                .restaurantId(restaurantId)
                .categoryVersion(0L)
                .categories(List.of(payload))
                .build();

        when(restaurant.getId()).thenReturn(new RestaurantId(restaurantId));

        updateCategoriesCommandHandler.updateCategories(command);

        verify(restaurant, times(1)).updateCategories(anyList(), eq(0L));
        verify(restaurantRepository, times(1)).saveRestaurant(restaurant);
    }

    @Test
    public void testUpdateCategories_RestaurantNotFound() {
        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

        UpdateCategoriesCommand command = UpdateCategoriesCommand.builder()
                .restaurantId(restaurantId)
                .categoryVersion(0L)
                .categories(List.of())
                .build();

        assertThrows(RestaurantNotFoundException.class, () -> updateCategoriesCommandHandler.updateCategories(command));

        verify(restaurant, never()).updateCategories(anyList(), anyLong());
        verify(restaurantRepository, never()).saveRestaurant(any(Restaurant.class));
    }
}
