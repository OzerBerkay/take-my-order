package com.berkay.restaurant.service.application.security;

import com.berkay.application.security.JwtAuthenticationToken;
import com.berkay.restaurant.service.domain.ports.output.repository.RolePermissionQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantAuthServiceTest {

    @Mock
    private RolePermissionQueryPort rolePermissionQueryPort;

    @InjectMocks
    private RestaurantAuthService restaurantAuthService;

    private JwtAuthenticationToken m2mAuth;

    @BeforeEach
    void setUp() {
        m2mAuth = mock(JwtAuthenticationToken.class);
    }

    @Test
    void hasPermission_ShouldReturnTrue_OnlyForSyncRole_WhenUserTypeIsM2M() {
        when(m2mAuth.getUserType()).thenReturn("M2M");

        boolean syncResult = restaurantAuthService.hasPermission(m2mAuth, "restaurant_service_can_sync_roles");
        boolean createResult = restaurantAuthService.hasPermission(m2mAuth, "can_manage_restaurant");

        assertTrue(syncResult);
        org.junit.jupiter.api.Assertions.assertFalse(createResult);
        verifyNoInteractions(rolePermissionQueryPort);
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenNotJwtAuthToken() {
        Authentication auth = mock(Authentication.class);
        boolean result = restaurantAuthService.hasPermission(auth, "can_create_restaurant");
        org.junit.jupiter.api.Assertions.assertFalse(result);
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenUserTypeInternal_AndNoPermission() {
        com.berkay.application.security.JwtAuthenticationToken auth = mock(com.berkay.application.security.JwtAuthenticationToken.class);
        when(auth.getUserType()).thenReturn("INTERNAL");
        when(auth.getRoleIds()).thenReturn(null);

        boolean result = restaurantAuthService.hasPermission(auth, "can_create_restaurant");
        org.junit.jupiter.api.Assertions.assertFalse(result);
    }
}
