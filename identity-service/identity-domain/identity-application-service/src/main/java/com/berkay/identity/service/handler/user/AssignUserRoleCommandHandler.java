package com.berkay.identity.service.handler.user;

import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.AssignUserRoleCommand;
import com.berkay.identity.service.handler.helper.UserUpdateIntentHelper;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignUserRoleCommandHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserUpdateIntentHelper userUpdateIntentHelper;
    private final TokenRevocationPort tokenRevocationPort;
    private final IdentityProviderPort identityProviderPort;

    public void assign(AssignUserRoleCommand command) {
        log.info("Assigning role {} to user {}", command.roleId(), command.userId());

        // 1. Check if user exists
        User user = userRepository.findById(new UserId(command.userId()))
                .orElseThrow(() -> new IdentityDomainException("User not found: " + command.userId()));

        // 2. Check if role exists
        Role role = roleRepository.findById(new RoleId(command.roleId()))
                .orElseThrow(() -> new IdentityDomainException("Role not found: " + command.roleId()));

        // 3. Fetch Requester Roles from DB for authorization (No need to fetch the full User!)
        if (command.requesterUserType() == null || command.requesterRoleIds() == null) {
             throw new IdentityDomainException("Requester details are missing!");
        }

        java.util.List<Role> requesterRoles = roleRepository.findAllById(command.requesterRoleIds());

        // 4. DB-based Authorization Check
        boolean isGlobalRole = role.getOrganizationalUnitId() == null;
        boolean hasPermission = false;

        if (isGlobalRole) {
            // Global role -> Must be INTERNAL
            if (!com.berkay.identity.service.domain.valueobject.UserType.INTERNAL.equals(command.requesterUserType())) {
                throw new IdentityDomainException("Only INTERNAL users can assign global roles!");
            }
            
            hasPermission = requesterRoles.stream()
                    .filter(r -> r.getOrganizationalUnitId() == null) // Filter global roles
                    .flatMap(r -> r.getPermissions().stream())
                    .anyMatch(p -> p.getCode().equals("can_assign_role") && p.isActive());
        } else {
            // Restaurant-specific role -> Must be MERCHANT
            if (!com.berkay.identity.service.domain.valueobject.UserType.MERCHANT.equals(command.requesterUserType())) {
                throw new IdentityDomainException("Only MERCHANT users can assign restaurant-specific roles! Admin cannot touch restaurant roles.");
            }

            hasPermission = requesterRoles.stream()
                    .filter(r -> r.getOrganizationalUnitId() != null && r.getOrganizationalUnitId().equals(role.getOrganizationalUnitId()))
                    .flatMap(r -> r.getPermissions().stream())
                    .anyMatch(p -> p.getCode().equals("can_assign_role") && p.isActive());
        }

        if (!hasPermission) {
            throw new IdentityDomainException("Requester does not have 'can_assign_role' permission for this context!");
        }

        // 5. Target User Membership Check
        if (isGlobalRole) {
            if (!com.berkay.identity.service.domain.valueobject.UserType.INTERNAL.equals(user.getUserType())) {
                throw new IdentityDomainException("Global roles can only be assigned to INTERNAL users!");
            }
        } else {
            if (!user.getOrganizationalUnitIds().contains(role.getOrganizationalUnitId())) {
                throw new IdentityDomainException("Cannot assign a restaurant-specific role to a user who is not a member of that restaurant!");
            }
        }

        java.util.List<String> updatedRoleIds = user.getRoles().stream()
                .map(r -> r.getId().getValue().toString())
                .collect(Collectors.toList());
        if (!updatedRoleIds.contains(role.getId().getValue().toString())) {
            updatedRoleIds.add(role.getId().getValue().toString());
        }

        java.util.List<String> updatedOrgUnitIds = user.getOrganizationalUnitIds().stream()
                .map(java.util.UUID::toString)
                .collect(Collectors.toList());

        // 6. Create Intent and update user
        UserUpdateIntent intent = userUpdateIntentHelper.createIntent(
                user.getId().getValue(),
                "UPDATE_USER", // Command Type
                "ROLE_ASSIGNED: " + role.getId().getValue(),
                ""
        );

        try {
            identityProviderPort.updateUserRolesAndBranches(user.getExternalId(), updatedRoleIds, updatedOrgUnitIds);
            userUpdateIntentHelper.markKeycloakDone(intent.getId().getValue());
        } catch (Exception e) {
            log.error("Failed to update user roles in Keycloak, intent remains STARTED for recovery. UserId: {}", command.userId(), e);
            throw new IdentityDomainException("Failed to update user roles in Keycloak. System will retry automatically.");
        }

        userUpdateIntentHelper.completeIntent(intent.getId().getValue(), u -> {
            u.addRole(role);
        });

        tokenRevocationPort.revokeAccessToken(command.userId());
        log.info("Successfully assigned role {} to user {} and revoked their access token.", command.roleId(), command.userId());
    }
}
