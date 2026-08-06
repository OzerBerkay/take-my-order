package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.create.AddPersonnelCommand;
import com.berkay.restaurant.service.domain.dto.create.AddPersonnelResponse;
import com.berkay.restaurant.service.domain.dto.query.UserValidationResponse;
import com.berkay.restaurant.service.domain.entity.RestaurantPersonnel;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.ports.output.api.IdentityServiceApiPort;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantPersonnelRepository;
import com.berkay.restaurant.service.domain.valueobject.RestaurantPersonnelId;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantPersonnelEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddPersonnelCommandHandler {

    private final RestaurantPersonnelRepository restaurantPersonnelRepository;
    private final IdentityServiceApiPort identityServiceApiPort;
    private final RestaurantOutboxHelper restaurantOutboxHelper;

    @Transactional
    public AddPersonnelResponse addPersonnel(AddPersonnelCommand command) {
        // 1. Check if user exists and is valid in Identity Service
        UserValidationResponse validationResponse = identityServiceApiPort.validateUserForPersonnel(command.getEmail());
        if (!validationResponse.isValid()) {
            throw new RestaurantDomainException("Validation failed for user email " + command.getEmail() + ": " + validationResponse.getErrorMessage());
        }

        // 2. Check if already added
        if (restaurantPersonnelRepository.existsByRestaurantIdAndUserId(command.getRestaurantId(), validationResponse.getUserId())) {
            throw new RestaurantDomainException("User with email " + command.getEmail() + " is already a personnel in this restaurant.");
        }

        // 3. Save RestaurantPersonnel
        RestaurantPersonnel personnel = RestaurantPersonnel.builder()
                .restaurantPersonnelId(new RestaurantPersonnelId(java.util.UUID.randomUUID()))
                .restaurantId(new RestaurantId(command.getRestaurantId()))
                .userId(validationResponse.getUserId())
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        RestaurantPersonnel savedPersonnel = restaurantPersonnelRepository.save(personnel);

        // 4. Save Outbox Event
        RestaurantPersonnelEventPayload payload = RestaurantPersonnelEventPayload.builder()
                .restaurantId(command.getRestaurantId().toString())
                .userId(validationResponse.getUserId().toString())
                .addedByMerchantId(command.getAddedByMerchantId().toString())
                .createdAt(savedPersonnel.getCreatedAt())
                .eventType("PERSONNEL_ADDED")
                .build();
        restaurantOutboxHelper.savePersonnelOutboxMessage(payload);

        return AddPersonnelResponse.builder()
                .personnelId(savedPersonnel.getId().getValue())
                .restaurantId(savedPersonnel.getRestaurantId().getValue())
                .message("Personnel added successfully")
                .build();
    }
}
