package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.delete.RemovePersonnelCommand;
import com.berkay.restaurant.service.domain.dto.delete.RemovePersonnelResponse;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantPersonnelRepository;
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
public class RemovePersonnelCommandHandler {

    private final RestaurantPersonnelRepository restaurantPersonnelRepository;
    private final RestaurantOutboxHelper restaurantOutboxHelper;

    @Transactional
    public RemovePersonnelResponse removePersonnel(RemovePersonnelCommand command) {
        // 1. Check if the personnel exists in this restaurant
        if (!restaurantPersonnelRepository.existsByRestaurantIdAndUserId(command.getRestaurantId(), command.getUserId())) {
            throw new RestaurantDomainException("User with ID " + command.getUserId() + " is not a personnel in restaurant " + command.getRestaurantId());
        }

        // 2. Delete the record from the database
        restaurantPersonnelRepository.deleteByRestaurantIdAndUserId(command.getRestaurantId(), command.getUserId());
        log.info("Physically deleted personnel {} from restaurant {}", command.getUserId(), command.getRestaurantId());

        // 3. Save Outbox Event (Identity service will listen to this and remove the roles)
        RestaurantPersonnelEventPayload payload = RestaurantPersonnelEventPayload.builder()
                .restaurantId(command.getRestaurantId().toString())
                .userId(command.getUserId().toString())
                .addedByMerchantId(command.getRemovedByMerchantId().toString())
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .eventType("PERSONNEL_REMOVED")
                .build();
        
        restaurantOutboxHelper.savePersonnelOutboxMessage(payload);

        return RemovePersonnelResponse.builder()
                .restaurantId(command.getRestaurantId())
                .userId(command.getUserId())
                .message("Personnel removed successfully")
                .build();
    }
}
