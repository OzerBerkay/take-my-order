package com.berkay.identity.service.application.listener;

import com.berkay.identity.service.domain.entity.OrganizationalUnit;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.IntentId;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitId;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.OrganizationalUnitRepository;
import com.berkay.kafka.order.avro.model.RestaurantPersonnelAvroModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantPersonnelMessageListenerImplTest {

    @Mock
    private UserUpdateIntentHelper userUpdateIntentHelper;

    @Mock
    private OrganizationalUnitRepository organizationalUnitRepository;

    @InjectMocks
    private RestaurantPersonnelMessageListenerImpl messageListener;

    private UUID userId;
    private UUID restaurantId;
    private RestaurantPersonnelAvroModel payload;
    private OrganizationalUnit orgUnit;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();

        payload = mock(RestaurantPersonnelAvroModel.class);
        when(payload.getUserId()).thenReturn(userId);
        when(payload.getRestaurantId()).thenReturn(restaurantId);

        orgUnit = OrganizationalUnit.builder()
                .id(new OrganizationalUnitId(restaurantId))
                .name("Test Restaurant")
                .build();
    }

    @Test
    void shouldSuccessfullyProcessPersonnelRemoved() {
        // Arrange
        when(organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))).thenReturn(Optional.of(orgUnit));
        
        UserUpdateIntent intent = UserUpdateIntent.builder()
                .intentId(new IntentId(UUID.randomUUID()))
                .build();
                
        when(userUpdateIntentHelper.createIntent(eq(userId), eq("REMOVE_RESTAURANT_PERSONNEL"), eq("{}"), eq("{}"))).thenReturn(intent);

        // We capture the Consumer that modifies the user
        doAnswer(invocation -> {
            Consumer<User> consumer = invocation.getArgument(1);
            User mockUser = mock(User.class);
            
            // Create a role that belongs to this orgUnit
            Role roleToKeep = mock(Role.class);
            when(roleToKeep.getOrganizationalUnitId()).thenReturn(UUID.randomUUID());
            
            Role roleToRemove = mock(Role.class);
            when(roleToRemove.getOrganizationalUnitId()).thenReturn(restaurantId);
            
            when(mockUser.getRoles()).thenReturn(java.util.List.of(roleToKeep, roleToRemove));
            
            consumer.accept(mockUser);
            
            verify(mockUser, times(1)).removeOrganizationalUnit(orgUnit);
            verify(mockUser, times(1)).removeRole(roleToRemove);
            verify(mockUser, never()).removeRole(roleToKeep);
            
            return null;
        }).when(userUpdateIntentHelper).completeIntent(eq(intent.getId().getValue()), any());

        // Act
        messageListener.personnelRemoved(payload);

        // Assert
        verify(organizationalUnitRepository, times(1)).findById(new OrganizationalUnitId(restaurantId));
        verify(userUpdateIntentHelper, times(1)).createIntent(eq(userId), eq("REMOVE_RESTAURANT_PERSONNEL"), eq("{}"), eq("{}"));
        verify(userUpdateIntentHelper, times(1)).completeIntent(eq(intent.getId().getValue()), any());
    }

    @Test
    void shouldThrowExceptionWhenOrgUnitNotFoundOnPersonnelRemoved() {
        // Arrange
        when(organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))).thenReturn(Optional.empty());

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> {
            messageListener.personnelRemoved(payload);
        });

        verify(organizationalUnitRepository, times(1)).findById(new OrganizationalUnitId(restaurantId));
        verify(userUpdateIntentHelper, never()).createIntent(any(), any(), any(), any());
        verify(userUpdateIntentHelper, never()).completeIntent(any(), any());
    }
}
