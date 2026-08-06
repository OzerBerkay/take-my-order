package com.berkay.identity.service.mapper;

import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.outbox.model.DomainEventType;
import com.berkay.identity.service.outbox.model.role.PermissionPayload;
import com.berkay.identity.service.outbox.model.role.RoleEventPayload;
import com.berkay.identity.service.outbox.model.role.RolePayload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleDataMapper {

    // savedRole parametresi alıyor ki JPA'nın güncellediği Version verisini Kafka'ya basabilelim.
    public RoleEventPayload roleCreatedEventToRoleEventPayload(Role savedRole) {
        return RoleEventPayload.builder()
                .eventType(DomainEventType.ROLE_CREATED.name())
                .role(RolePayload.builder()
                        .id(savedRole.getId().getValue())
                        .name(savedRole.getName())
                        .userType(savedRole.getUserType().name())
                        .organizationalUnitId(savedRole.getOrganizationalUnitId())
                        .version(savedRole.getVersion()) // JPA'nın atadığı güncel versiyon (Örn: 1)
                        .permissions(mapPermissions(savedRole.getPermissions()))
                        .build())
                .build();
    }

    public RoleEventPayload roleUpdatedEventToRoleEventPayload(Role savedRole) {
        return RoleEventPayload.builder()
                .eventType(DomainEventType.ROLE_UPDATED.name())
                .role(RolePayload.builder()
                        .id(savedRole.getId().getValue())
                        .name(savedRole.getName())
                        .userType(savedRole.getUserType().name())
                        .organizationalUnitId(savedRole.getOrganizationalUnitId())
                        .version(savedRole.getVersion()) // JPA'nın atadığı güncel versiyon (Örn: 2)
                        .permissions(mapPermissions(savedRole.getPermissions()))
                        .build())
                .build();
    }

    public RoleEventPayload roleDeletedEventToRoleEventPayload(Role deletedRole) {
        // Döküman Madde 6: DELETE işleminde versiyona ve detaylara ihtiyacımız yok. Sadece silinen ID yeterli.
        return RoleEventPayload.builder()
                .eventType(DomainEventType.ROLE_DELETED.name())
                .role(RolePayload.builder()
                        .id(deletedRole.getId().getValue())
                        .build())
                .build();
    }

    // Refactor: create ve update'in ortak kullanması için mapPermissions'ı list alacak şekilde güncelledik
    private List<PermissionPayload> mapPermissions(List<Permission> permissions) {
        return permissions.stream()
                .map(permission -> PermissionPayload.builder()
                        .id(permission.getId().getValue())
                        .code(permission.getCode())
                        .domain(permission.getDomain().name())
                        .build())
                .collect(Collectors.toList());
    }

    public com.berkay.identity.service.dto.query.RoleResponse roleToRoleResponse(Role role) {
        return com.berkay.identity.service.dto.query.RoleResponse.builder()
                .id(role.getId().getValue())
                .name(role.getName())
                .isStatic(role.isStatic())
                .organizationalUnitId(role.getOrganizationalUnitId())
                .userType(role.getUserType().name())
                .createdAt(role.getCreatedAt())
                .permissions(role.getPermissions().stream()
                        .map(this::permissionToPermissionResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public com.berkay.identity.service.dto.query.PermissionResponse permissionToPermissionResponse(Permission permission) {
        return com.berkay.identity.service.dto.query.PermissionResponse.builder()
                .id(permission.getId().getValue())
                .name(permission.getCode())
                .description(permission.getDescription())
                .active(permission.isActive())
                .isRestricted(permission.isRestricted())
                .build();
    }
}