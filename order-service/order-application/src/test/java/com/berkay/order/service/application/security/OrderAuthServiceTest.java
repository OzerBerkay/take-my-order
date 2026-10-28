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
    private com.berkay.order.service.domain.ports.output.repository.OrderRepository orderRepository;

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

    @Test
    void isOrderCustomer_ReturnsTrue_WhenUserIsOrderOwner() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        JwtAuthenticationToken userAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(userAuth.getInternalId()).thenReturn(customerId);

        com.berkay.order.service.domain.ports.output.repository.OrderAuthProjection projection =
                org.mockito.Mockito.mock(com.berkay.order.service.domain.ports.output.repository.OrderAuthProjection.class);
        when(projection.getCustomerId()).thenReturn(customerId);
        when(orderRepository.findAuthProjectionById(orderId)).thenReturn(java.util.Optional.of(projection));

        boolean result = orderAuthService.isOrderCustomer(userAuth, orderId);

        assertTrue(result);
    }

    @Test
    void isOrderCustomer_ReturnsFalse_WhenUserIsNotOrderOwner() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        JwtAuthenticationToken userAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(userAuth.getInternalId()).thenReturn(otherId);

        com.berkay.order.service.domain.ports.output.repository.OrderAuthProjection projection =
                org.mockito.Mockito.mock(com.berkay.order.service.domain.ports.output.repository.OrderAuthProjection.class);
        when(projection.getCustomerId()).thenReturn(customerId);
        when(orderRepository.findAuthProjectionById(orderId)).thenReturn(java.util.Optional.of(projection));

        boolean result = orderAuthService.isOrderCustomer(userAuth, orderId);

        assertFalse(result);
    }

    @Test
    void isOrderCustomer_ReturnsFalse_WhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        JwtAuthenticationToken userAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(orderRepository.findAuthProjectionById(orderId)).thenReturn(java.util.Optional.empty());

        boolean result = orderAuthService.isOrderCustomer(userAuth, orderId);

        assertFalse(result);
    }

    @Test
    void isOrderCustomer_ReturnsFalse_WhenOrderIdIsNull() {
        JwtAuthenticationToken userAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);

        boolean result = orderAuthService.isOrderCustomer(userAuth, null);

        assertFalse(result);
    }

    @Test
    void hasPermissionForRestaurant_ReturnsTrue_WhenUserIsOwnerOfTargetRestaurant() {
        UUID restaurantId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        JwtAuthenticationToken userAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(userAuth.getRoleIds()).thenReturn(List.of(roleId));
        when(rolePermissionQueryPort.getOrganizationalUnitIdByRoleId(roleId)).thenReturn(restaurantId);
        when(rolePermissionQueryPort.getPermissionCodesByRoleId(roleId)).thenReturn(Set.of("can_reply_restaurant_review"));

        boolean result = orderAuthService.hasPermissionForRestaurant(userAuth, "can_reply_restaurant_review", restaurantId);

        assertTrue(result);
    }

    @Test
    void hasPermissionForRestaurant_ReturnsFalse_ForReviewReply_WhenUserIsPlatformAdmin() {
        UUID restaurantId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        JwtAuthenticationToken adminAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(adminAuth.getRoleIds()).thenReturn(List.of(roleId));
        // Platform admin has null organizational unit id
        when(rolePermissionQueryPort.getOrganizationalUnitIdByRoleId(roleId)).thenReturn(null);
        when(rolePermissionQueryPort.getPermissionCodesByRoleId(roleId)).thenReturn(Set.of("can_read_all_orders", "can_reply_restaurant_review"));

        boolean result = orderAuthService.hasPermissionForRestaurant(adminAuth, "can_reply_restaurant_review", restaurantId);

        assertFalse(result, "Platform admin without restaurant org unit must NEVER be allowed to reply to restaurant reviews");
    }

    @Test
    void hasPermissionForRestaurant_ReturnsFalse_ForReviewReply_WhenUserBelongsToDifferentRestaurant() {
        UUID restaurantId = UUID.randomUUID();
        UUID differentRestaurantId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        JwtAuthenticationToken userAuth = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        when(userAuth.getRoleIds()).thenReturn(List.of(roleId));
        when(rolePermissionQueryPort.getOrganizationalUnitIdByRoleId(roleId)).thenReturn(differentRestaurantId);

        boolean result = orderAuthService.hasPermissionForRestaurant(userAuth, "can_reply_restaurant_review", restaurantId);

        assertFalse(result, "Owner of restaurant A must not be allowed to reply to reviews of restaurant B");
    }
}
