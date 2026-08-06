package com.berkay.identity.service.handler.role;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.event.RoleUpdatedEvent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.DomainType;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.command.role.UpdateRoleCommand;
import com.berkay.identity.service.dto.command.role.UpdateRoleResponse;
import com.berkay.identity.service.mapper.RoleDataMapper;
import com.berkay.identity.service.outbox.helper.RoleOutboxHelper;
import com.berkay.identity.service.outbox.model.role.RoleEventPayload;
import com.berkay.identity.service.ports.output.config.RoleSecurityPolicyPort;
import com.berkay.identity.service.ports.output.repository.PermissionRepository;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.security.SecurityContextPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateRoleCommandHandler {

    private final IdentityDomainService identityDomainService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SecurityContextPort securityContextPort;
    private final RoleSecurityPolicyPort roleSecurityPolicyPort;
    private final RoleDataMapper roleDataMapper;
    private final RoleOutboxHelper roleOutboxHelper;

    @Transactional
    public UpdateRoleResponse updateRole(UpdateRoleCommand command) {
        // 1. Rolü DB'den getir
        UserType callerUserType = securityContextPort.getCurrentUserType();

        // 2. Caller UserType ile Hedef organizationalUnitId'nin mantıksal uyumu kontrolü
        validateUserTypeAndOrganizationalUnitIdLogic(callerUserType, command.getOrganizationalUnitId());

        // 3. Spoofing Koruması: MERCHANT kendi context'i dışında işlem yapamaz
        java.util.Set<java.util.UUID> allowedOrganizationalUnitIds = securityContextPort.getAllowedOrganizationalUnitIds();
        if (command.getOrganizationalUnitId() != null && !allowedOrganizationalUnitIds.contains(command.getOrganizationalUnitId())) {
            throw new IdentityDomainException("Spoofing detected! The provided organizationalUnitId does not match your allowed contexts!");
        }

        Role role = roleRepository.findById(new RoleId(command.getRoleId()))
                .orElseThrow(() -> new IdentityDomainException("Role not found with id: " + command.getRoleId()));

        // 4. Güncellenmek istenen Rolün DB'deki organizationalUnitId'si ile kullanıcının belirttiği organizationalUnitId aynı mı?
        if ((role.getOrganizationalUnitId() == null && command.getOrganizationalUnitId() != null) ||
                (role.getOrganizationalUnitId() != null && !role.getOrganizationalUnitId().equals(command.getOrganizationalUnitId()))) {
            throw new IdentityDomainException("You cannot update a role that belongs to a different organizationalUnitId or is global!");
        }

        // 5. Aynı context içinde isim çakışması kontrolü
        if (roleRepository.existsByNameAndOrganizationalUnitIdAndIdNot(command.getName(), command.getOrganizationalUnitId(), command.getRoleId())) {
            throw new IdentityDomainException("Another role with name " + command.getName() + " already exists in context " + command.getOrganizationalUnitId());
        }

        // 5. Yeni Permission'ları DB'den çek
        List<Permission> newPermissions = permissionRepository.findActivePermissionsByIds(command.getPermissionIds());
        if (newPermissions.size() != command.getPermissionIds().size()) {
            throw new IdentityDomainException("Some permissions are invalid, inactive, or not found!");
        }

        // 6. Caller'ın YAML'daki Allowed Domain listesini çek
        List<DomainType> allowedDomains = roleSecurityPolicyPort.getAllowedDomainsForUserType(callerUserType);

        // 7. Caller'ın kendi yetkilerini çek (Alt küme kuralı için)
        List<Permission> callerPermissions = permissionRepository.findActivePermissionsByRoleIds(securityContextPort.getCurrentUserRoleIds());

        // 8. Domain Service çağrısı (Static check, immutability check ve domain check burada gerçekleşir)
        RoleUpdatedEvent event = identityDomainService
                .validateAndInitiateRoleUpdate(role, command.getName(), newPermissions, callerPermissions, allowedDomains);

        // 8. Önce DB'ye yaz (Versiyon artırılsın)
        Role savedRole = roleRepository.save(role);

        // 9. Event Payload'unu Mapper üzerinden yarat
        RoleEventPayload eventPayload = roleDataMapper.roleUpdatedEventToRoleEventPayload(savedRole);

        // 10. Outbox'a yaz
        roleOutboxHelper.saveRoleOutboxMessage(eventPayload);
        log.info("Role updated successfully with ID: {}", savedRole.getId().getValue());

        return UpdateRoleResponse.builder()
                .roleId(savedRole.getId().getValue())
                .build();
    }

    /**
     * İsteği atan kişinin (Caller), hedef organizationalUnitId üzerinde işlem yapmaya yetkisi var mı?
     */
    private void validateUserTypeAndOrganizationalUnitIdLogic(UserType callerUserType, java.util.UUID targetOrganizationalUnitId) {
        if (callerUserType == UserType.INTERNAL) {
            if (targetOrganizationalUnitId != null) {
                throw new IdentityDomainException("Internal users can only manage global roles (organizationalUnitId must be null)!");
            }
        } else if (callerUserType == UserType.MERCHANT) {
            if (targetOrganizationalUnitId == null) {
                throw new IdentityDomainException("Merchant users must specify an organizationalUnitId for the role!");
            }
        } else if (callerUserType == UserType.CUSTOMER) {
            throw new IdentityDomainException("Customers are not allowed to update roles.");
        }
    }
}