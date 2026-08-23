package com.berkay.payment.service.application.security;

import com.berkay.application.security.JwtAuthenticationToken;
import com.berkay.payment.service.domain.ports.output.repository.RolePermissionQueryPort;
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
public class PaymentAuthServiceTest {

    @Mock
    private RolePermissionQueryPort rolePermissionQueryPort;

    @InjectMocks
    private PaymentAuthService paymentAuthService;

    private JwtAuthenticationToken m2mAuth;

    @BeforeEach
    void setUp() {
        m2mAuth = mock(JwtAuthenticationToken.class);
    }

    @Test
    void hasPermission_ShouldReturnTrue_OnlyForSyncRole_WhenUserTypeIsM2M() {
        when(m2mAuth.getUserType()).thenReturn("M2M");

        boolean syncResult = paymentAuthService.hasPermission(m2mAuth, "payment_service_can_sync_roles");
        boolean createResult = paymentAuthService.hasPermissionForWallet(m2mAuth, "can_process_payment", UUID.randomUUID());

        assertTrue(syncResult);
        org.junit.jupiter.api.Assertions.assertFalse(createResult);
        verifyNoInteractions(rolePermissionQueryPort);
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenNotJwtAuthToken() {
        Authentication auth = mock(Authentication.class);
        boolean result = paymentAuthService.hasPermission(auth, "can_process_payment");
        org.junit.jupiter.api.Assertions.assertFalse(result);
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenUserTypeInternal_AndNoPermission() {
        com.berkay.application.security.JwtAuthenticationToken auth = mock(com.berkay.application.security.JwtAuthenticationToken.class);
        when(auth.getUserType()).thenReturn("INTERNAL");
        when(auth.getRoleIds()).thenReturn(null);

        boolean result = paymentAuthService.hasPermission(auth, "can_process_payment");
        org.junit.jupiter.api.Assertions.assertFalse(result);
    }
}
