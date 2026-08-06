package com.berkay.identity.service.dataaccess.role.mapper;

import com.berkay.identity.service.dataaccess.permission.mapper.PermissionDataAccessMapper;
import com.berkay.identity.service.dataaccess.role.entity.RoleEntity;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleDataAccessMapper {

    private final PermissionDataAccessMapper permissionDataAccessMapper;

    public RoleEntity roleToRoleEntity(Role role) {
        return RoleEntity.builder()
                .id(role.getId() != null ? role.getId().getValue() : null)
                .name(role.getName())
                .userType(role.getUserType())
                .organizationalUnitId(role.getOrganizationalUnitId())
                .isStatic(role.isStatic())
                .createdByUserId(role.getCreatedByUserId() != null ? role.getCreatedByUserId().getValue() : null)
                .version(role.getVersion())
                .build();
    }

    public Role roleEntityToRole(RoleEntity roleEntity, java.util.List<com.berkay.identity.service.domain.entity.Permission> permissions) {
        return Role.builder()
                .roleId(new RoleId(roleEntity.getId()))
                .name(roleEntity.getName())
                .userType(roleEntity.getUserType())
                .organizationalUnitId(roleEntity.getOrganizationalUnitId())
                .isStatic(roleEntity.isStatic())
                .createdByUserId(roleEntity.getCreatedByUserId() != null ? new UserId(roleEntity.getCreatedByUserId()) : null)
                .permissions(permissions)
                .version(roleEntity.getVersion())
                .build();
    }
}