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

import java.util.Objects;

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

        UserType callerUserType = securityContextPort.getCurrentUserType();

        // 2. Spoofing Koruması: MERCHANT kendi organizationalUnitId'si dışında işlem yapamaz
        java.util.Set<java.util.UUID> allowedOrganizationalUnitIds = securityContextPort.getAllowedOrganizationalUnitIds();
        if (command.getOrganizationalUnitId() != null && !allowedOrganizationalUnitIds.contains(command.getOrganizationalUnitId())) {
            throw new IdentityDomainException("Spoofing detected! The provided organizationalUnitId does not match your allowed contexts!");
        }

        // 3. INTERNAL kullanıcıların Global rolleri (organizationalUnitId == null) silebilmesi gerekir.
        // MERCHANT kullanıcıların ise SADECE kendi restaurant (context) rollerini silebilmesi gerekir.
        if (UserType.INTERNAL.equals(callerUserType)) {
            if (role.getOrganizationalUnitId() != null) {
                throw new IdentityDomainException("Internal users can only delete global roles (organizationalUnitId must be null)!");
            }
        } else if (UserType.MERCHANT.equals(callerUserType)) {
            if (role.getOrganizationalUnitId() == null || !role.getOrganizationalUnitId().equals(command.getOrganizationalUnitId())) {
                throw new IdentityDomainException("You cannot delete a role that belongs to a different organizationalUnitId or is global!");
            }
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