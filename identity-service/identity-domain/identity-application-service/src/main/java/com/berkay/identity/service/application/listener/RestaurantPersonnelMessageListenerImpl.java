package com.berkay.identity.service.application.listener;

import com.berkay.identity.service.domain.entity.OrganizationalUnit;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitId;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.input.message.listener.restaurant.RestaurantPersonnelMessageListener;
import com.berkay.identity.service.ports.output.repository.OrganizationalUnitRepository;
import com.berkay.kafka.order.avro.model.RestaurantPersonnelAvroModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantPersonnelMessageListenerImpl implements RestaurantPersonnelMessageListener {

    private final UserUpdateIntentHelper userUpdateIntentHelper;
    private final OrganizationalUnitRepository organizationalUnitRepository;

    @Override
    @Transactional
    public void personnelAdded(RestaurantPersonnelAvroModel payload) {
        log.info("Received RestaurantPersonnelAddedEvent for user {} in restaurant {}", payload.getUserId(), payload.getRestaurantId());

        UUID userId = payload.getUserId();
        UUID restaurantId = payload.getRestaurantId();

        // Check if OrgUnit exists
        OrganizationalUnit orgUnit = organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))
                .orElseThrow(() -> new IdentityDomainException("OrganizationalUnit not found for restaurant: " + restaurantId));

        UserUpdateIntent intent = userUpdateIntentHelper.createIntent(
                userId,
                "ASSIGN_RESTAURANT_PERSONNEL",
                "{}", "{}" // simple snapshots
        );

        userUpdateIntentHelper.completeIntent(intent.getId().getValue(), user -> {
            user.addOrganizationalUnit(orgUnit);
        });

        log.info("Successfully added user {} to restaurant {}", userId, restaurantId);
    }
}
