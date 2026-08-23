package com.berkay.order.service.application.security;

import com.berkay.order.service.domain.ports.output.repository.RolePermissionQueryPort;
import com.berkay.application.security.JwtAuthenticationToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderAuthServiceTest {

    @Mock
    private RolePermissionQueryPort rolePermissionQueryPort;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private OrderAuthService orderAuthService;

    @Test
    void hasPermission_ReturnsTrue_WhenUserHasPermission() {
        String requiredPermission = "can_create_order";
        UUID roleId = UUID.randomUUID();
        JwtAuthenticationToken userAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(userAuth.getRoleIds()).thenReturn(List.of(roleId));
        when(rolePermissionQueryPort.getPermissionCodesByRoleId(roleId)).thenReturn(Set.of(requiredPermission));

        boolean result = orderAuthService.hasPermission(userAuth, requiredPermission);

        assertTrue(result);
    }
    
    @Test
    void hasPermission_ReturnsTrue_OnlyForSyncRole_WhenUserTypeIsM2M() {
        JwtAuthenticationToken m2mAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(m2mAuth.getUserType()).thenReturn("M2M");

        boolean syncResult = orderAuthService.hasPermission(m2mAuth, "order_service_can_sync_roles");
        boolean createResult = orderAuthService.hasPermission(m2mAuth, "can_create_order");

        assertTrue(syncResult);
        assertFalse(createResult);
        org.mockito.Mockito.verifyNoInteractions(rolePermissionQueryPort);
    }

    @Test
    void hasPermission_ProceedsToNormalChecks_WhenUserTypeIsNotM2M() {
        JwtAuthenticationToken userAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(userAuth.getUserType()).thenReturn("CUSTOMER");
        
        String requiredPermission = "can_create_order";
        UUID roleId = UUID.randomUUID();
        when(userAuth.getRoleIds()).thenReturn(List.of(roleId));
        when(rolePermissionQueryPort.getPermissionCodesByRoleId(roleId)).thenReturn(Set.of(requiredPermission));

        boolean result = orderAuthService.hasPermission(userAuth, requiredPermission);

        assertTrue(result);
    }

    @Test
    void isOwner_ReturnsTrue_WhenUserIsOwner() {
        UUID customerId = UUID.randomUUID();
        JwtAuthenticationToken userAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(userAuth.getInternalId()).thenReturn(customerId);

        boolean result = orderAuthService.isOwner(userAuth, customerId);

        assertTrue(result);
    }

    @Test
    void isOwner_ReturnsFalse_WhenUserIsNotOwner() {
        UUID customerId = UUID.randomUUID();
        UUID anotherId = UUID.randomUUID();
        JwtAuthenticationToken userAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(userAuth.getInternalId()).thenReturn(anotherId);

        boolean result = orderAuthService.isOwner(userAuth, customerId);

        assertFalse(result);
    }
}
