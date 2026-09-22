package com.berkay.identity.service.handler.user;

import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.command.UpdateMerchantUserRolesCommand;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateMerchantUserRolesCommandHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserUpdateIntentHelper userUpdateIntentHelper;
    private final TokenRevocationPort tokenRevocationPort;
    private final IdentityProviderPort identityProviderPort;

    public void update(UpdateMerchantUserRolesCommand command) {
        log.info("Updating roles in batch for user: {} in orgUnit: {}", command.userId(), command.orgUnitId());

        // 1. Check if user exists
        User user = userRepository.findById(new UserId(command.userId()))
                .orElseThrow(() -> new IdentityDomainException("User not found: " + command.userId()));

        // 2. Validate requester context
        if (command.requesterUserType() == null || command.requesterRoleIds() == null) {
            throw new IdentityDomainException("Requester details are missing!");
        }

        if (!UserType.MERCHANT.equals(command.requesterUserType())) {
            throw new IdentityDomainException("Only MERCHANT users can update restaurant-specific roles! Admin cannot touch restaurant roles.");
        }

        // 3. DB-based Authorization Check for requester
        List<Role> requesterRoles = roleRepository.findAllById(command.requesterRoleIds());
        boolean hasPermission = requesterRoles.stream()
                .filter(r -> r.getOrganizationalUnitId() != null && r.getOrganizationalUnitId().equals(command.orgUnitId()))
                .flatMap(r -> r.getPermissions().stream())
                .anyMatch(p -> p.getCode().equals("can_assign_role") && p.isActive());

        if (!hasPermission) {
            throw new IdentityDomainException("Requester does not have 'can_assign_role' permission for this context!");
        }

        // 4. Target User Membership Check
        if (!UserType.MERCHANT.equals(user.getUserType())) {
            throw new IdentityDomainException("Cannot update restaurant roles for non-merchant users!");
        }

        if (!user.getOrganizationalUnitIds().contains(command.orgUnitId())) {
            throw new IdentityDomainException("Cannot assign restaurant-specific roles to a user who is not a member of that restaurant!");
        }

        // 5. Target Roles Fetch & Validation
        List<UUID> distinctRoleIds = command.roleIds() != null
                ? command.roleIds().stream().distinct().toList()
                : Collections.emptyList();

        List<Role> targetRoles;
        if (!distinctRoleIds.isEmpty()) {
            targetRoles = roleRepository.findAllById(distinctRoleIds);
            if (targetRoles.size() != distinctRoleIds.size()) {
                throw new IdentityDomainException("One or more roles were not found!");
            }
            for (Role role : targetRoles) {
                if (role.isStatic()) {
                    throw new IdentityDomainException("Cannot manually assign static base roles!");
                }
                if (role.getOrganizationalUnitId() == null || !role.getOrganizationalUnitId().equals(command.orgUnitId())) {
                    throw new IdentityDomainException("Role does not belong to the target organizational unit!");
                }
            }
        } else {
            targetRoles = Collections.emptyList();
        }

        // 6. Compute updated roles list for Keycloak sync
        // Keep static roles (e.g. MERCHANT_BASE) and roles belonging to other orgUnits
        List<String> updatedRoleIds = user.getRoles().stream()
                .filter(r -> r.isStatic() || !command.orgUnitId().equals(r.getOrganizationalUnitId()))
                .map(r -> r.getId().getValue().toString())
                .collect(Collectors.toList());

        for (Role r : targetRoles) {
            String rIdStr = r.getId().getValue().toString();
            if (!updatedRoleIds.contains(rIdStr)) {
                updatedRoleIds.add(rIdStr);
            }
        }

        List<String> updatedOrgUnitIds = user.getOrganizationalUnitIds().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());

        // 7. Create Intent
        String oldSnapshot = user.getRoles().stream()
                .filter(r -> command.orgUnitId().equals(r.getOrganizationalUnitId()))
                .map(r -> r.getId().getValue().toString())
                .collect(Collectors.joining(","));

        String newSnapshot = distinctRoleIds.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));

        UserUpdateIntent intent = userUpdateIntentHelper.createIntent(
                user.getId().getValue(),
                "UPDATE_USER",
                "ROLES_BATCH_UPDATED: " + command.orgUnitId(),
                "OLD: [" + oldSnapshot + "], NEW: [" + newSnapshot + "]"
        );

        // 8. Keycloak Sync
        try {
            identityProviderPort.updateUserRolesAndBranches(user.getExternalId(), updatedRoleIds, updatedOrgUnitIds);
            userUpdateIntentHelper.markKeycloakDone(intent.getId().getValue());
        } catch (Exception e) {
            log.error("Failed to update user roles in Keycloak, intent remains STARTED for recovery. UserId: {}", command.userId(), e);
            throw new IdentityDomainException("Failed to update user roles in Keycloak. System will retry automatically.");
        }

        // 9. Complete Intent & Update User in DB
        userUpdateIntentHelper.completeIntent(intent.getId().getValue(), u -> {
            u.updateRolesForOrganizationalUnit(command.orgUnitId(), targetRoles);
        });

        // 10. Revoke Access Token
        tokenRevocationPort.revokeAccessToken(command.userId());
        log.info("Successfully updated roles in batch for user {} in orgUnit {} and revoked their access token.",
                command.userId(), command.orgUnitId());
    }
}
