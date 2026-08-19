package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.delete.RemovePersonnelCommand;
import com.berkay.restaurant.service.domain.dto.delete.RemovePersonnelResponse;
import com.berkay.restaurant.service.domain.entity.RestaurantPersonnel;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantPersonnelRepository;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RemovePersonnelCommandHandlerTest {

    @Mock
    private RestaurantPersonnelRepository restaurantPersonnelRepository;

    @Mock
    private RestaurantOutboxHelper restaurantOutboxHelper;

    @InjectMocks
    private RemovePersonnelCommandHandler removePersonnelCommandHandler;

    private UUID restaurantId;
    private UUID userId;
    private RemovePersonnelCommand removePersonnelCommand;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        removePersonnelCommand = RemovePersonnelCommand.builder()
                .restaurantId(restaurantId)
                .userId(userId)
                .removedByMerchantId(UUID.randomUUID())
                .build();
    }

    @Test
    void shouldRemovePersonnelSuccessfully() {
        // Arrange
        when(restaurantPersonnelRepository.existsByRestaurantIdAndUserId(restaurantId, userId)).thenReturn(true);
        doNothing().when(restaurantPersonnelRepository).deleteByRestaurantIdAndUserId(restaurantId, userId);
        doNothing().when(restaurantOutboxHelper).savePersonnelOutboxMessage(any());

        // Act
        RemovePersonnelResponse response = removePersonnelCommandHandler.removePersonnel(removePersonnelCommand);

        // Assert
        assertNotNull(response);
        assertEquals(restaurantId, response.getRestaurantId());
        assertEquals(userId, response.getUserId());
        assertEquals("Personnel removed successfully", response.getMessage());

        verify(restaurantPersonnelRepository, times(1)).existsByRestaurantIdAndUserId(restaurantId, userId);
        verify(restaurantPersonnelRepository, times(1)).deleteByRestaurantIdAndUserId(restaurantId, userId);
        verify(restaurantOutboxHelper, times(1)).savePersonnelOutboxMessage(any());
    }

    @Test
    void shouldThrowExceptionWhenPersonnelNotFoundInRestaurant() {
        // Arrange
        when(restaurantPersonnelRepository.existsByRestaurantIdAndUserId(restaurantId, userId)).thenReturn(false);

        // Act & Assert
        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class, () -> {
            removePersonnelCommandHandler.removePersonnel(removePersonnelCommand);
        });

        assertEquals("User with ID " + userId + " is not a personnel in restaurant " + restaurantId, exception.getMessage());
        
        verify(restaurantPersonnelRepository, times(1)).existsByRestaurantIdAndUserId(restaurantId, userId);
        verify(restaurantPersonnelRepository, never()).deleteByRestaurantIdAndUserId(any(), any());
        verify(restaurantOutboxHelper, never()).savePersonnelOutboxMessage(any());
    }

    @Test
    void shouldPropagateExceptionWhenDatabaseDeletionFails() {
        // Arrange
        when(restaurantPersonnelRepository.existsByRestaurantIdAndUserId(restaurantId, userId)).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(restaurantPersonnelRepository).deleteByRestaurantIdAndUserId(restaurantId, userId);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            removePersonnelCommandHandler.removePersonnel(removePersonnelCommand);
        });

        assertEquals("Database error", exception.getMessage());

        verify(restaurantPersonnelRepository, times(1)).existsByRestaurantIdAndUserId(restaurantId, userId);
        verify(restaurantPersonnelRepository, times(1)).deleteByRestaurantIdAndUserId(restaurantId, userId);
        verify(restaurantOutboxHelper, never()).savePersonnelOutboxMessage(any());
    }
}
