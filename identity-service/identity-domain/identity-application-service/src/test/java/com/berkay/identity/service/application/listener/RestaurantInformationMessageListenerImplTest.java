package com.berkay.identity.service.application.listener;

import com.berkay.identity.service.domain.constants.RoleConstants;
import com.berkay.identity.service.domain.dto.message.RestaurantInformationEventPayload;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.ports.output.repository.PermissionRepository;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantInformationMessageListenerImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private com.berkay.identity.service.ports.output.repository.TokenRevocationPort tokenRevocationPort;

    @Mock
    private com.berkay.identity.service.ports.output.repository.IdentityProviderPort identityProviderPort;

    @Mock
    private com.berkay.identity.service.ports.output.repository.OrganizationalUnitRepository organizationalUnitRepository;

    @Mock
    private com.berkay.identity.service.outbox.helper.RoleOutboxHelper roleOutboxHelper;

    @Mock
    private com.berkay.identity.service.mapper.RoleDataMapper roleDataMapper;

    @InjectMocks
    private RestaurantInformationMessageListenerImpl listener;

    private UUID merchantId;
    private UUID restaurantId;
    private RestaurantInformationEventPayload payload;

    @BeforeEach
    void setUp() {
        merchantId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        payload = RestaurantInformationEventPayload.builder()
                .merchantId(merchantId)
                .restaurantId(restaurantId)
                .active(true)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Test
    void shouldIgnore_WhenMerchantIdIsNull() {
        RestaurantInformationEventPayload invalidPayload = RestaurantInformationEventPayload.builder()
                .restaurantId(restaurantId)
                .merchantId(null)
                .build();

        listener.restaurantCreated(invalidPayload);

        verify(userRepository, never()).findById(any());
        verify(roleRepository, never()).save(any());
    }

    @Test
    void shouldIgnore_WhenUserNotFound() {
        when(userRepository.findById(new UserId(merchantId))).thenReturn(Optional.empty());

        listener.restaurantCreated(payload);

        verify(userRepository, times(1)).findById(new UserId(merchantId));
        verify(roleRepository, never()).save(any());
        verify(organizationalUnitRepository, never()).save(any());
    }

    @Test
    void shouldIgnore_WhenUserIsNotMerchant() {
        User customer = User.builder()
                .userId(new UserId(merchantId))
                .userType(UserType.CUSTOMER)
                .build();
        
        when(userRepository.findById(new UserId(merchantId))).thenReturn(Optional.of(customer));

        listener.restaurantCreated(payload);

        verify(roleRepository, never()).save(any());
        verify(organizationalUnitRepository, never()).save(any());
    }

    @Test
    void shouldCreateRoleAndAssign_WhenRoleDoesNotExist() {
        User merchant = User.builder()
                .userId(new UserId(merchantId))
                .userType(UserType.MERCHANT)
                .build();

        when(userRepository.findById(new UserId(merchantId))).thenReturn(Optional.of(merchant));
        when(roleRepository.findByNameAndOrganizationalUnitId(RoleConstants.RESTAURANT_OWNER, restaurantId))
                .thenReturn(Optional.empty());

        // Mock permission repository
        List<String> permissionCodes = RoleConstants.ROLE_PERMISSIONS.get(RoleConstants.RESTAURANT_OWNER);
        for (String code : permissionCodes) {
            Permission p = Permission.builder().code(code).build();
            when(permissionRepository.findByCode(code)).thenReturn(Optional.of(p));
        }

        Role savedRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .name(RoleConstants.RESTAURANT_OWNER)
                .build();

        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        com.berkay.identity.service.outbox.model.role.RoleEventPayload mockRoleEventPayload = com.berkay.identity.service.outbox.model.role.RoleEventPayload.builder().build();
        when(roleDataMapper.roleCreatedEventToRoleEventPayload(any(Role.class))).thenReturn(mockRoleEventPayload);

        listener.restaurantCreated(payload);

        // Verify role creation
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        
        Role capturedRole = roleCaptor.getValue();
        assertEquals(RoleConstants.RESTAURANT_OWNER, capturedRole.getName());
        assertTrue(capturedRole.isStatic());
        assertEquals(restaurantId, capturedRole.getOrganizationalUnitId());
        assertEquals(UserType.MERCHANT, capturedRole.getUserType());

        assertTrue(merchant.getRoles().contains(savedRole));
        assertTrue(merchant.getOrganizationalUnitIds().contains(restaurantId));
        
        verify(identityProviderPort).updateUserRolesAndBranches(eq(merchant.getExternalId()), anyList(), anyList());
        verify(tokenRevocationPort).revokeAccessToken(merchantId);
    }

    @Test
    void shouldAssignExistingRole_WhenRoleExists() {
        User merchant = User.builder()
                .userId(new UserId(merchantId))
                .userType(UserType.MERCHANT)
                .build();

        Role existingRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .name(RoleConstants.RESTAURANT_OWNER)
                .build();

        when(userRepository.findById(new UserId(merchantId))).thenReturn(Optional.of(merchant));
        when(roleRepository.findByNameAndOrganizationalUnitId(RoleConstants.RESTAURANT_OWNER, restaurantId))
                .thenReturn(Optional.of(existingRole));



        listener.restaurantCreated(payload);

        verify(roleRepository, never()).save(any());
        
        assertTrue(merchant.getRoles().contains(existingRole));
        assertTrue(merchant.getOrganizationalUnitIds().contains(restaurantId));
        
        verify(identityProviderPort).updateUserRolesAndBranches(eq(merchant.getExternalId()), anyList(), anyList());
        verify(tokenRevocationPort).revokeAccessToken(merchantId);
    }

    @Test
    void shouldThrowException_WhenRequiredPermissionsAreMissing() {
        User merchant = User.Builder.from(User.builder()
                .userId(new UserId(merchantId))
                .userType(UserType.MERCHANT)
                .build()).build();

        when(userRepository.findById(new UserId(merchantId))).thenReturn(Optional.of(merchant));
        when(roleRepository.findByNameAndOrganizationalUnitId(RoleConstants.RESTAURANT_OWNER, restaurantId))
                .thenReturn(Optional.empty());

        // Mock permission repository to return empty for everything
        when(permissionRepository.findByCode(any())).thenReturn(Optional.empty());

        assertThrows(IdentityDomainException.class, () -> listener.restaurantCreated(payload));
    }
}
