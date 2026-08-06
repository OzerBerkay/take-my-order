package com.berkay.identity.service.handler.permission;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.PermissionId;
import com.berkay.identity.service.dto.command.permission.UpdatePermissionCommand;
import com.berkay.identity.service.dto.command.permission.UpdatePermissionResponse;
import com.berkay.identity.service.ports.output.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePermissionCommandHandler {

    private final IdentityDomainService identityDomainService;
    private final PermissionRepository permissionRepository;
    private final com.berkay.identity.service.outbox.helper.PermissionOutboxHelper permissionOutboxHelper;

    @Transactional
    public UpdatePermissionResponse updatePermission(UpdatePermissionCommand command) {
        log.info("Updating permission with id: {}", command.getPermissionId());

        Permission permission = permissionRepository.findById(new PermissionId(command.getPermissionId()))
                .orElseThrow(() -> new IdentityDomainException("Permission not found with id: " + command.getPermissionId()));

        boolean isActiveBefore = permission.isActive();

        identityDomainService.validateAndInitiatePermissionUpdate(permission, command.getDescription(), command.getActive());

        Permission savedPermission = permissionRepository.save(permission);
        
        log.info("Permission updated successfully with ID: {}", savedPermission.getId().getValue());

        if (isActiveBefore != savedPermission.isActive()) {
            log.info("Permission isActive status changed. Writing PERMISSION_UPDATED event to outbox.");
            com.berkay.identity.service.outbox.model.role.PermissionPayload permissionPayload = com.berkay.identity.service.outbox.model.role.PermissionPayload.builder()
                    .id(savedPermission.getId().getValue())
                    .code(savedPermission.getCode())
                    .domain(savedPermission.getDomain().name())
                    .isActive(savedPermission.isActive())
                    .isRestricted(savedPermission.isRestricted())
                    .createdAt(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"))))
                    .updatedAt(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"))))
                    .build();

            com.berkay.identity.service.outbox.model.permission.PermissionEventPayload eventPayload = com.berkay.identity.service.outbox.model.permission.PermissionEventPayload.builder()
                    .eventType("PERMISSION_UPDATED")
                    .permission(permissionPayload)
                    .build();

            permissionOutboxHelper.savePermissionOutboxMessage(eventPayload);
        }

        return UpdatePermissionResponse.builder()
                .permissionId(savedPermission.getId().getValue())
                .build();
    }
}
