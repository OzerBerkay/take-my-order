package com.berkay.identity.service.application.listener;

import com.berkay.identity.service.domain.constants.PermissionConstants;
import com.berkay.identity.service.domain.constants.RoleConstants;
import com.berkay.identity.service.domain.dto.message.RestaurantInformationEventPayload;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.ports.input.message.listener.restaurant.RestaurantInformationMessageListener;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.PermissionRepository;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.berkay.identity.service.domain.entity.OrganizationalUnit;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitId;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitType;
import com.berkay.identity.service.ports.output.repository.OrganizationalUnitRepository;
import com.berkay.identity.service.outbox.helper.RoleOutboxHelper;
import com.berkay.identity.service.mapper.RoleDataMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantInformationMessageListenerImpl implements RestaurantInformationMessageListener {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final IdentityProviderPort identityProviderPort;
    private final TokenRevocationPort tokenRevocationPort;

    private final RoleOutboxHelper roleOutboxHelper;
    private final RoleDataMapper roleDataMapper;

    @Override
    @Transactional
    public void restaurantCreated(RestaurantInformationEventPayload payload) {
        if (payload.getMerchantId() == null) {
            log.warn("Restaurant created event received without merchantId for restaurant: {}. Ignoring.", payload.getRestaurantId());
            return;
        }

        log.info("Processing restaurant created event for restaurant: {}, merchant: {}", payload.getRestaurantId(), payload.getMerchantId());

        UserId merchantId = new UserId(payload.getMerchantId());
        Optional<User> userOpt = userRepository.findById(merchantId);

        if (userOpt.isEmpty()) {
            log.warn("Merchant user not found for ID: {}. Cannot process restaurant created event.", payload.getMerchantId());
            return;
        }

        User merchant = userOpt.get();

        // Ensure user is actually a merchant
        if (!UserType.MERCHANT.equals(merchant.getUserType())) {
            log.warn("User {} is not a MERCHANT. Skipping role assignment and replica creation.", merchantId.getValue());
            return;
        }

        // 1. Save OrganizationalUnit Replica
        String orgUnitName = payload.getName() != null && !payload.getName().isEmpty() 
                ? payload.getName() 
                : "Restaurant " + payload.getRestaurantId().toString().substring(0, 8);

        OrganizationalUnit orgUnit = OrganizationalUnit.builder()
                .id(new OrganizationalUnitId(payload.getRestaurantId()))
                .name(orgUnitName)
                .type(OrganizationalUnitType.MERCHANT)
                .build();
        organizationalUnitRepository.save(orgUnit);
        log.info("Saved organizational unit replica for restaurant: {}", payload.getRestaurantId());

        // 1. Create RESTAURANT_OWNER role for this specific restaurant if it doesn't exist
        Role restaurantOwnerRole = getOrCreateRestaurantOwnerRole(payload);

        boolean hasRoleAlready = merchant.getRoles().stream()
                .anyMatch(r -> r.getId().equals(restaurantOwnerRole.getId()));
        
        if (!hasRoleAlready) {
            merchant.addSystemRole(restaurantOwnerRole);
        }
        
        merchant.addOrganizationalUnit(orgUnit);
        userRepository.save(merchant);

        // Sync changes to Keycloak so JWT token gets the updated roles and organizational units
        try {
            List<String> roleIds = merchant.getRoles().stream()
                    .map(r -> r.getId().getValue().toString())
                    .toList();
            List<String> orgUnitIds = merchant.getOrganizationalUnitIds().stream()
                    .map(UUID::toString)
                    .toList();
            identityProviderPort.updateUserRolesAndBranches(merchant.getExternalId(), roleIds, orgUnitIds);
            tokenRevocationPort.revokeAccessToken(merchantId.getValue());
            log.info("Successfully synced roles and branches to Keycloak and revoked access token for merchant {}", merchantId.getValue());
        } catch (Exception e) {
            log.error("Failed to sync updated roles and branches to Keycloak for merchant {}. Kafka will retry.", merchantId.getValue(), e);
            throw e; // Propagate to let Kafka retry
        }

        log.info("Successfully assigned RESTAURANT_OWNER role to merchant {} for restaurant {}", merchantId.getValue(), payload.getRestaurantId());
    }

    private Role getOrCreateRestaurantOwnerRole(RestaurantInformationEventPayload payload) {
        // Name is unique per organizational unit
        String roleName = RoleConstants.RESTAURANT_OWNER;
        
        Optional<Role> existingRole = roleRepository.findByNameAndOrganizationalUnitId(roleName, payload.getRestaurantId());
        if (existingRole.isPresent()) {
            return existingRole.get();
        }

        // We will create the dynamic role
        List<Permission> rolePermissions = new ArrayList<>();
        List<String> permissionCodes = RoleConstants.ROLE_PERMISSIONS.getOrDefault(RoleConstants.RESTAURANT_OWNER, List.of());
        
        for (String code : permissionCodes) {
            permissionRepository.findByCode(code).ifPresent(rolePermissions::add);
        }

        if (rolePermissions.isEmpty()) {
            throw new IdentityDomainException("Cannot create RESTAURANT_OWNER role because required permissions are missing in DB.");
        }

        Role newRole = Role.builder()
                .roleId(new RoleId(UuidCreator.getTimeOrderedEpoch()))
                .name(roleName)
                .userType(UserType.MERCHANT)
                .organizationalUnitId(payload.getRestaurantId())
                .isStatic(true) // Document rule: RESTAURANT_OWNER core role should be static true
                .createdByUserId(new UserId(payload.getMerchantId()))
                .permissions(rolePermissions)
                .build();

        newRole.initializeRole();
        Role savedRole = roleRepository.save(newRole);

        // Publish to outbox so restaurant-service and other replicas get this role
        com.berkay.identity.service.outbox.model.role.RoleEventPayload eventPayload = roleDataMapper.roleCreatedEventToRoleEventPayload(savedRole);
        roleOutboxHelper.saveRoleOutboxMessage(eventPayload);

        return savedRole;
    }
}
