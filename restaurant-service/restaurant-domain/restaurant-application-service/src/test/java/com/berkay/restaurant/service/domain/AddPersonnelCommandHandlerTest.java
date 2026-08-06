package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.create.AddPersonnelCommand;
import com.berkay.restaurant.service.domain.dto.create.AddPersonnelResponse;
import com.berkay.restaurant.service.domain.dto.query.UserValidationResponse;
import com.berkay.restaurant.service.domain.entity.RestaurantPersonnel;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.ports.output.api.IdentityServiceApiPort;
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
class AddPersonnelCommandHandlerTest {

    @Mock
    private RestaurantPersonnelRepository restaurantPersonnelRepository;

    @Mock
    private IdentityServiceApiPort identityServiceApiPort;

    @Mock
    private RestaurantOutboxHelper restaurantOutboxHelper;

    @InjectMocks
    private AddPersonnelCommandHandler addPersonnelCommandHandler;

    private UUID restaurantId;
    private UUID merchantId;
    private UUID newPersonnelId;
    private String email;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        merchantId = UUID.randomUUID();
        newPersonnelId = UUID.randomUUID();
        email = "personnel@test.com";
    }

    @Test
    void shouldAddPersonnelSuccessfully() {
        AddPersonnelCommand command = AddPersonnelCommand.builder()
                .restaurantId(restaurantId)
                .addedByMerchantId(merchantId)
                .email(email)
                .build();

        UserValidationResponse validationResponse = UserValidationResponse.builder()
                .userId(newPersonnelId)
                .valid(true)
                .build();

        when(identityServiceApiPort.validateUserForPersonnel(email)).thenReturn(validationResponse);
        when(restaurantPersonnelRepository.existsByRestaurantIdAndUserId(restaurantId, newPersonnelId)).thenReturn(false);

        RestaurantPersonnel savedPersonnel = RestaurantPersonnel.builder()
                .restaurantPersonnelId(new com.berkay.restaurant.service.domain.valueobject.RestaurantPersonnelId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(restaurantId))
                .userId(newPersonnelId)
                .build();
        when(restaurantPersonnelRepository.save(any(RestaurantPersonnel.class))).thenReturn(savedPersonnel);

        AddPersonnelResponse response = addPersonnelCommandHandler.addPersonnel(command);

        assertNotNull(response);
        assertEquals(restaurantId, response.getRestaurantId());
        assertEquals("Personnel added successfully", response.getMessage());

        verify(restaurantOutboxHelper, times(1)).savePersonnelOutboxMessage(any());
    }

    @Test
    void shouldThrowExceptionWhenUserInvalid() {
        AddPersonnelCommand command = AddPersonnelCommand.builder()
                .restaurantId(restaurantId)
                .addedByMerchantId(merchantId)
                .email(email)
                .build();

        UserValidationResponse validationResponse = UserValidationResponse.builder()
                .valid(false)
                .errorMessage("User not active")
                .build();

        when(identityServiceApiPort.validateUserForPersonnel(email)).thenReturn(validationResponse);

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class, () -> {
            addPersonnelCommandHandler.addPersonnel(command);
        });

        assertTrue(exception.getMessage().contains("User not active"));
        verify(restaurantPersonnelRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyPersonnel() {
        AddPersonnelCommand command = AddPersonnelCommand.builder()
                .restaurantId(restaurantId)
                .addedByMerchantId(merchantId)
                .email(email)
                .build();

        UserValidationResponse validationResponse = UserValidationResponse.builder()
                .userId(newPersonnelId)
                .valid(true)
                .build();

        when(identityServiceApiPort.validateUserForPersonnel(email)).thenReturn(validationResponse);
        when(restaurantPersonnelRepository.existsByRestaurantIdAndUserId(restaurantId, newPersonnelId)).thenReturn(true);

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class, () -> {
            addPersonnelCommandHandler.addPersonnel(command);
        });

        assertTrue(exception.getMessage().contains("is already a personnel in this restaurant"));
        verify(restaurantPersonnelRepository, never()).save(any());
    }
}
