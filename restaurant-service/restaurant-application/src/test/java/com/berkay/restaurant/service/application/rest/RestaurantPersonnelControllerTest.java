package com.berkay.restaurant.service.application.rest;

import com.berkay.application.security.JwtAuthenticationToken;
import com.berkay.restaurant.service.application.security.RestaurantAuthService;
import com.berkay.restaurant.service.domain.AddPersonnelCommandHandler;
import com.berkay.restaurant.service.domain.RemovePersonnelCommandHandler;
import com.berkay.restaurant.service.domain.dto.delete.RemovePersonnelCommand;
import com.berkay.restaurant.service.domain.dto.delete.RemovePersonnelResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantPersonnelControllerTest {

    @Mock
    private AddPersonnelCommandHandler addPersonnelCommandHandler;

    @Mock
    private RemovePersonnelCommandHandler removePersonnelCommandHandler;

    @Mock
    private RestaurantAuthService restaurantAuthService;

    @InjectMocks
    private RestaurantPersonnelController restaurantPersonnelController;

    private UUID restaurantId;
    private UUID userId;
    private UUID merchantId;
    
    @Mock
    private SecurityContext securityContext;

    @Mock
    private JwtAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        merchantId = UUID.randomUUID();
        
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRemovePersonnelSuccessfullyWhenAuthorized() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(restaurantAuthService.hasPermissionForRestaurant(authentication, "can_remove_personnel", restaurantId)).thenReturn(true);
        when(authentication.getInternalId()).thenReturn(merchantId);

        RemovePersonnelResponse expectedResponse = RemovePersonnelResponse.builder()
                .restaurantId(restaurantId)
                .userId(userId)
                .message("Success")
                .build();
                
        when(removePersonnelCommandHandler.removePersonnel(any(RemovePersonnelCommand.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<RemovePersonnelResponse> responseEntity = restaurantPersonnelController.removePersonnel(restaurantId, userId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        
        verify(restaurantAuthService, times(1)).hasPermissionForRestaurant(authentication, "can_remove_personnel", restaurantId);
        verify(removePersonnelCommandHandler, times(1)).removePersonnel(argThat(cmd -> 
            cmd.getRestaurantId().equals(restaurantId) && 
            cmd.getUserId().equals(userId) && 
            cmd.getRemovedByMerchantId().equals(merchantId)
        ));
    }

    @Test
    void shouldReturnForbiddenWhenUserLacksPermission() {
        // Arrange
        Authentication normalAuth = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(normalAuth);
        when(restaurantAuthService.hasPermissionForRestaurant(normalAuth, "can_remove_personnel", restaurantId)).thenReturn(false);

        // Act
        ResponseEntity<RemovePersonnelResponse> responseEntity = restaurantPersonnelController.removePersonnel(restaurantId, userId);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        verify(removePersonnelCommandHandler, never()).removePersonnel(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenMerchantIdIsNull() {
        // Arrange
        Authentication normalAuth = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(normalAuth);
        when(restaurantAuthService.hasPermissionForRestaurant(normalAuth, "can_remove_personnel", restaurantId)).thenReturn(true);
        // Authentication is NOT instance of JwtAuthenticationToken, so extractUserIdFromAuthentication returns null

        // Act
        ResponseEntity<RemovePersonnelResponse> responseEntity = restaurantPersonnelController.removePersonnel(restaurantId, userId);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        verify(removePersonnelCommandHandler, never()).removePersonnel(any());
    }
}
