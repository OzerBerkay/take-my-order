package com.berkay.identity.service.dataaccess.permission.mapper;

import com.berkay.identity.service.dataaccess.permission.entity.PermissionEntity;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.valueobject.PermissionId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionDataAccessMapper {

    public List<PermissionEntity> permissionListToPermissionEntityList(List<Permission> permissions) {
        if (permissions == null) return null;
        return permissions.stream()
                .map(p -> PermissionEntity.builder()
                        .id(p.getId().getValue())
                        .code(p.getCode())
                        .description(p.getDescription())
                        .domain(p.getDomain())
                        .active(p.isActive()) // Lombok active field'ı için isActive() üretir
                        .isRestricted(p.isRestricted())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Permission> permissionEntityListToPermissionList(List<PermissionEntity> permissionEntities) {
        if (permissionEntities == null) return null;
        return permissionEntities.stream()
                .map(this::permissionEntityToPermission)
                .collect(Collectors.toList());
    }

    public PermissionEntity permissionToPermissionEntity(Permission permission) {
        if (permission == null) return null;
        return PermissionEntity.builder()
                .id(permission.getId() != null ? permission.getId().getValue() : null)
                .code(permission.getCode())
                .description(permission.getDescription())
                .domain(permission.getDomain())
                .active(permission.isActive())
                .isRestricted(permission.isRestricted())
                .build();
    }

    public Permission permissionEntityToPermission(PermissionEntity permissionEntity) {
        if (permissionEntity == null) return null;
        return Permission.builder()
                .permissionId(new PermissionId(permissionEntity.getId()))
                .code(permissionEntity.getCode())
                .description(permissionEntity.getDescription())
                .domain(permissionEntity.getDomain())
                .active(permissionEntity.isActive())
                .isRestricted(permissionEntity.isRestricted())
                .build();
    }
}