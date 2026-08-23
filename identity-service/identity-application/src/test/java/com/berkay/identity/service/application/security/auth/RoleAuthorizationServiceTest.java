package com.berkay.identity.service.application.security.auth;

import com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleAuthorizationServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleAuthorizationService roleAuthService;

    private JwtAuthenticationToken m2mAuth;
    private JwtAuthenticationToken customerAuth;

    @BeforeEach
    void setUp() {
        m2mAuth = mock(JwtAuthenticationToken.class);
        customerAuth = mock(JwtAuthenticationToken.class);
    }

    @Test
    void hasPermission_ShouldReturnTrue_WhenUserTypeIsM2M() {
        when(m2mAuth.getUserType()).thenReturn(UserType.M2M);

        boolean result = roleAuthService.hasPermission(m2mAuth, "can_create_role");

        assertTrue(result);
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void hasPermission_ShouldProceedToNormalChecks_WhenUserTypeIsNotM2M() {
        when(customerAuth.getUserType()).thenReturn(UserType.CUSTOMER);
        
        // Customer role can't manage roles anyway, so it should return false
        boolean result = roleAuthService.hasPermission(customerAuth, "can_create_role");

        assertFalse(result);
    }
}
