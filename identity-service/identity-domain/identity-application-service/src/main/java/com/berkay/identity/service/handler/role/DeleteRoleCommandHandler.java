package com.berkay.identity.service.handler.role;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.event.RoleDeletedEvent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.dto.command.role.DeleteRoleCommand;
import com.berkay.identity.service.dto.command.role.DeleteRoleResponse;
import com.berkay.identity.service.mapper.RoleDataMapper;
import com.berkay.identity.service.outbox.helper.RoleOutboxHelper;
import com.berkay.identity.service.outbox.model.role.RoleEventPayload;
import com.berkay.identity.service.domain.valueobject.UserType;
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
public class DeleteRoleCommandHandler {

    private final IdentityDomainService identityDomainService;
    private final RoleRepository roleRepository;
    private final RoleDataMapper roleDataMapper;
    private final RoleOutboxHelper roleOutboxHelper;
    private final SecurityContextPort securityContextPort;

    @Transactional
    public DeleteRoleResponse deleteRole(DeleteRoleCommand command) {
        // 1. Rolü DB'den getir
        Role role = roleRepository.findById(new RoleId(command.getRoleId()))
                .orElseThrow(() -> new IdentityDomainException("Role not found with id: " + command.getRoleId()));

        // 2. Statik rol koruması (Fail-fast)
        if (role.isStatic()) {
            throw new IdentityDomainException("Static roles (is_static=true) cannot be deleted!");
        }

        UserType callerUserType = securityContextPort.getCurrentUserType();
        UUID targetOrgUnitId = role.getOrganizationalUnitId();

        List<UUID> callerRoleIds = securityContextPort.getCurrentUserRoleIds();
        List<Role> callerRoles = roleRepository.findAllById(callerRoleIds);

        if (UserType.INTERNAL.equals(callerUserType)) {
            if (targetOrgUnitId != null) {
                throw new IdentityDomainException("Internal users can only delete global roles (organizationalUnitId must be null)!");
            }
        } else if (UserType.MERCHANT.equals(callerUserType)) {
            if (targetOrgUnitId == null) {
                throw new IdentityDomainException("Merchant users cannot delete global roles!");
            }
            boolean hasContextRole = callerRoles.stream()
                    .anyMatch(r -> r.getOrganizationalUnitId() != null && r.getOrganizationalUnitId().equals(targetOrgUnitId));
            if (!hasContextRole) {
                throw new IdentityDomainException("Spoofing detected! The provided organizationalUnitId does not match your allowed contexts!");
            }
            if (command.getOrganizationalUnitId() != null && !targetOrgUnitId.equals(command.getOrganizationalUnitId())) {
                throw new IdentityDomainException("Provided organizationalUnitId does not match the role's organizationalUnitId!");
            }
        } else {
            throw new IdentityDomainException("Customers are not allowed to delete roles.");
        }

        // 3. Domain Service Çağrısı (Statik rol silinemez vs kuralı burada işletilir)
        RoleDeletedEvent event = identityDomainService.validateAndInitiateRoleDelete(role);

        // 4. Önce DB'den siliyoruz (Döküman Madde 5 - Repository'de cascade işlemleri DB foreign key veya JPA ile sağlanır)
        roleRepository.delete(role);

        // 5. Delete payload'unu mapper'a gönderiyoruz (Sadece ID basacak, versiyona gerek yok)
        RoleEventPayload eventPayload = roleDataMapper.roleDeletedEventToRoleEventPayload(role);

        // 6. Outbox'a yaz
        roleOutboxHelper.saveRoleOutboxMessage(eventPayload);
        log.info("Role deleted successfully with ID: {}", role.getId().getValue());

        return DeleteRoleResponse.builder()
                .roleId(role.getId().getValue())
                .build();
    }
}