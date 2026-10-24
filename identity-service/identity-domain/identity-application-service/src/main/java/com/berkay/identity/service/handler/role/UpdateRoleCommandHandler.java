package com.berkay.identity.service.handler.role;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.event.RoleUpdatedEvent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;

import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.command.role.UpdateRoleCommand;
import com.berkay.identity.service.dto.command.role.UpdateRoleResponse;
import com.berkay.identity.service.mapper.RoleDataMapper;
import com.berkay.identity.service.outbox.helper.RoleOutboxHelper;
import com.berkay.identity.service.outbox.model.role.RoleEventPayload;

import com.berkay.identity.service.ports.output.repository.PermissionRepository;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.security.SecurityContextPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateRoleCommandHandler {

    private final IdentityDomainService identityDomainService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SecurityContextPort securityContextPort;

    private final RoleDataMapper roleDataMapper;
    private final RoleOutboxHelper roleOutboxHelper;
    private final com.berkay.identity.service.ports.output.repository.TokenRevocationPort tokenRevocationPort;

    @Transactional
    public UpdateRoleResponse updateRole(UpdateRoleCommand command) {
        // 1. Rolü DB'den getir
        Role role = roleRepository.findById(new RoleId(command.getRoleId()))
                .orElseThrow(() -> new IdentityDomainException("Role not found with id: " + command.getRoleId()));

        // 2. Statik rol koruması (Fail-fast)
        if (role.isStatic()) {
            throw new IdentityDomainException("Static roles (is_static=true) cannot be updated!");
        }

        UserType callerUserType = securityContextPort.getCurrentUserType();
        UUID targetOrgUnitId = role.getOrganizationalUnitId();

        // 3. Caller UserType ile Hedef context uyumu kontrolü
        validateUserTypeAndContextLogic(callerUserType, targetOrgUnitId, command.getOrganizationalUnitId());

        // 4. Context & Spoofing Koruması: Çağrıcının hedef context'te bir rolü var mı kontrolü
        List<UUID> callerRoleIds = securityContextPort.getCurrentUserRoleIds();
        List<Role> callerRoles = roleRepository.findAllById(callerRoleIds);

        List<UUID> contextRoleIds;
        if (callerUserType == UserType.MERCHANT) {
            contextRoleIds = callerRoles.stream()
                    .filter(r -> r.getOrganizationalUnitId() != null && r.getOrganizationalUnitId().equals(targetOrgUnitId))
                    .map(r -> r.getId().getValue())
                    .toList();
            if (contextRoleIds.isEmpty()) {
                throw new IdentityDomainException("Spoofing detected! The provided organizationalUnitId does not match your allowed contexts!");
            }
        } else {
            contextRoleIds = callerRoles.stream()
                    .filter(r -> r.getOrganizationalUnitId() == null)
                    .map(r -> r.getId().getValue())
                    .toList();
        }

        // 5. Aynı context içinde isim çakışması kontrolü
        if (roleRepository.existsByNameAndOrganizationalUnitIdAndIdNot(command.getName(), targetOrgUnitId, command.getRoleId())) {
            throw new IdentityDomainException("Another role with name " + command.getName() + " already exists in context " + targetOrgUnitId);
        }

        // 6. Yeni Permission'ları DB'den çek
        List<Permission> newPermissions = permissionRepository.findActivePermissionsByIds(command.getPermissionIds());
        if (newPermissions.size() != command.getPermissionIds().size()) {
            throw new IdentityDomainException("Some permissions are invalid, inactive, or not found!");
        }

        // 7. Restricted permission kontrolü
        boolean hasRestricted = newPermissions.stream().anyMatch(Permission::isRestricted);
        if (hasRestricted) {
            throw new IdentityDomainException("Cannot assign restricted permissions to a custom role!");
        }

        // 8. Caller'ın hedef context'teki yetkilerini çek (Alt küme kuralı için)
        List<Permission> callerPermissions = permissionRepository.findActivePermissionsByRoleIds(contextRoleIds);

        // 9. Domain Service çağrısı (Static check, immutability check burada gerçekleşir)
        RoleUpdatedEvent event = identityDomainService
                .validateAndInitiateRoleUpdate(role, command.getName(), newPermissions, callerPermissions);

        // 10. Önce DB'ye yaz (Versiyon artırılsın)
        Role savedRole = roleRepository.save(role);

        // 11. Event Payload'unu Mapper üzerinden yarat
        RoleEventPayload eventPayload = roleDataMapper.roleUpdatedEventToRoleEventPayload(savedRole);

        // 12. Outbox'a yaz
        roleOutboxHelper.saveRoleOutboxMessage(eventPayload);
        tokenRevocationPort.revokeRole(savedRole.getId().getValue());
        log.info("Role updated successfully with ID: {} and revoked in Redis", savedRole.getId().getValue());

        return UpdateRoleResponse.builder()
                .roleId(savedRole.getId().getValue())
                .build();
    }

    private void validateUserTypeAndContextLogic(UserType callerUserType, UUID roleOrgUnitId, UUID commandOrgUnitId) {
        if (callerUserType == UserType.INTERNAL) {
            if (roleOrgUnitId != null) {
                throw new IdentityDomainException("Internal users can only manage global roles (organizationalUnitId must be null)!");
            }
            if (commandOrgUnitId != null) {
                throw new IdentityDomainException("Internal users can only manage global roles (organizationalUnitId must be null)!");
            }
        } else if (callerUserType == UserType.MERCHANT) {
            if (roleOrgUnitId == null) {
                throw new IdentityDomainException("Merchant users cannot manage global roles!");
            }
            if (commandOrgUnitId != null && !roleOrgUnitId.equals(commandOrgUnitId)) {
                throw new IdentityDomainException("Provided organizationalUnitId does not match the role's organizationalUnitId!");
            }
        } else if (callerUserType == UserType.CUSTOMER) {
            throw new IdentityDomainException("Customers are not allowed to update roles.");
        }
    }
}