package com.berkay.restaurant.service.dataaccess.restaurant.adapter;

import com.berkay.restaurant.service.dataaccess.restaurant.repository.RestaurantPersonnelJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantPersonnelRepositoryImplTest {

    @Mock
    private RestaurantPersonnelJpaRepository restaurantPersonnelJpaRepository;

    @InjectMocks
    private RestaurantPersonnelRepositoryImpl restaurantPersonnelRepositoryImpl;

    private UUID restaurantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void shouldReturnTrueWhenExistsByRestaurantIdAndUserId() {
        // Arrange
        when(restaurantPersonnelJpaRepository.existsByRestaurantIdAndUserId(restaurantId, userId)).thenReturn(true);

        // Act
        boolean exists = restaurantPersonnelRepositoryImpl.existsByRestaurantIdAndUserId(restaurantId, userId);

        // Assert
        assertTrue(exists);
        verify(restaurantPersonnelJpaRepository, times(1)).existsByRestaurantIdAndUserId(restaurantId, userId);
    }

    @Test
    void shouldReturnFalseWhenNotExistsByRestaurantIdAndUserId() {
        // Arrange
        when(restaurantPersonnelJpaRepository.existsByRestaurantIdAndUserId(restaurantId, userId)).thenReturn(false);

        // Act
        boolean exists = restaurantPersonnelRepositoryImpl.existsByRestaurantIdAndUserId(restaurantId, userId);

        // Assert
        assertFalse(exists);
        verify(restaurantPersonnelJpaRepository, times(1)).existsByRestaurantIdAndUserId(restaurantId, userId);
    }

    @Test
    void shouldCallDeleteByRestaurantIdAndUserId() {
        // Arrange
        doNothing().when(restaurantPersonnelJpaRepository).deleteByRestaurantIdAndUserId(restaurantId, userId);

        // Act
        restaurantPersonnelRepositoryImpl.deleteByRestaurantIdAndUserId(restaurantId, userId);

        // Assert
        verify(restaurantPersonnelJpaRepository, times(1)).deleteByRestaurantIdAndUserId(restaurantId, userId);
    }
}
