package com.berkay.identity.service.application.listener;

import com.berkay.identity.service.domain.entity.OrganizationalUnit;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.input.message.listener.restaurant.RestaurantPersonnelMessageListener;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.OrganizationalUnitRepository;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.kafka.order.avro.model.RestaurantPersonnelAvroModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantPersonnelMessageListenerImpl implements RestaurantPersonnelMessageListener {

    private final UserUpdateIntentHelper userUpdateIntentHelper;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final UserRepository userRepository;
    private final IdentityProviderPort identityProviderPort;
    private final TokenRevocationPort tokenRevocationPort;

    @Override
    public void personnelAdded(RestaurantPersonnelAvroModel payload) {
        log.info("Received RestaurantPersonnelAddedEvent for user {} in restaurant {}", payload.getUserId(), payload.getRestaurantId());

        UUID userId = payload.getUserId();
        UUID restaurantId = payload.getRestaurantId();

        // Check if OrgUnit exists
        OrganizationalUnit orgUnit = organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))
                .orElseThrow(() -> new IdentityDomainException("OrganizationalUnit not found for restaurant: " + restaurantId));

        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new IdentityDomainException("User not found: " + userId));

        List<String> roleIds = user.getRoles() != null
                ? user.getRoles().stream().map(r -> r.getId().getValue().toString()).toList()
                : List.of();

        List<String> updatedOrgUnitIds = new ArrayList<>();
        if (user.getOrganizationalUnitIds() != null) {
            updatedOrgUnitIds.addAll(user.getOrganizationalUnitIds().stream().map(UUID::toString).toList());
        }
        if (!updatedOrgUnitIds.contains(restaurantId.toString())) {
            updatedOrgUnitIds.add(restaurantId.toString());
        }

        UserUpdateIntent intent = userUpdateIntentHelper.createIntent(
                userId,
                "ASSIGN_RESTAURANT_PERSONNEL",
                "{}", "{}" // simple snapshots
        );

        try {
            identityProviderPort.updateUserRolesAndBranches(user.getExternalId(), roleIds, updatedOrgUnitIds);
            userUpdateIntentHelper.markKeycloakDone(intent.getId().getValue());
        } catch (Exception e) {
            log.warn("Failed to update user organizational units in Keycloak. UserId: {}, Error: {}", userId, e.getMessage());
            throw new IdentityDomainException("Failed to update user in Keycloak. System will retry automatically.", e);
        }

        userUpdateIntentHelper.completeIntent(intent.getId().getValue(), u -> {
            u.addOrganizationalUnit(orgUnit);
        });

        tokenRevocationPort.revokeAccessToken(userId);

        log.info("Successfully added user {} to restaurant {}, synced Keycloak and revoked access token", userId, restaurantId);
    }

    @Override
    public void personnelRemoved(RestaurantPersonnelAvroModel payload) {
        log.info("Received RestaurantPersonnelRemovedEvent for user {} in restaurant {}", payload.getUserId(), payload.getRestaurantId());

        UUID userId = payload.getUserId();
        UUID restaurantId = payload.getRestaurantId();

        // Check if OrgUnit exists
        OrganizationalUnit orgUnit = organizationalUnitRepository.findById(new OrganizationalUnitId(restaurantId))
                .orElseThrow(() -> new IdentityDomainException("OrganizationalUnit not found for restaurant: " + restaurantId));

        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new IdentityDomainException("User not found: " + userId));

        // Filter out roles of this specific restaurant
        List<String> updatedRoleIds = user.getRoles() != null
                ? user.getRoles().stream()
                        .filter(r -> r.getOrganizationalUnitId() == null || !r.getOrganizationalUnitId().equals(restaurantId))
                        .map(r -> r.getId().getValue().toString())
                        .toList()
                : List.of();

        // Filter out this restaurant from organizational units
        List<String> updatedOrgUnitIds = user.getOrganizationalUnitIds() != null
                ? user.getOrganizationalUnitIds().stream()
                        .filter(ouId -> !ouId.equals(restaurantId))
                        .map(UUID::toString)
                        .toList()
                : List.of();

        UserUpdateIntent intent = userUpdateIntentHelper.createIntent(
                userId,
                "REMOVE_RESTAURANT_PERSONNEL",
                "{}", "{}" // simple snapshots
        );

        try {
            identityProviderPort.updateUserRolesAndBranches(user.getExternalId(), updatedRoleIds, updatedOrgUnitIds);
            userUpdateIntentHelper.markKeycloakDone(intent.getId().getValue());
        } catch (Exception e) {
            log.warn("Failed to update user roles and branches in Keycloak for personnel removal. UserId: {}, Error: {}", userId, e.getMessage());
            throw new IdentityDomainException("Failed to update user in Keycloak. System will retry automatically.", e);
        }

        userUpdateIntentHelper.completeIntent(intent.getId().getValue(), u -> {
            // 1. Remove from organizational unit
            u.removeOrganizationalUnit(orgUnit);

            // 2. Remove all roles associated with this specific organizational unit
            List<Role> rolesToRemove = u.getRoles().stream()
                    .filter(r -> r.getOrganizationalUnitId() != null && r.getOrganizationalUnitId().equals(restaurantId))
                    .collect(Collectors.toList());

            rolesToRemove.forEach(u::removeRole);
        });

        tokenRevocationPort.revokeAccessToken(userId);

        log.info("Successfully removed user {} from restaurant {}, stripped associated roles, synced Keycloak and revoked access token", userId, restaurantId);
    }
}
