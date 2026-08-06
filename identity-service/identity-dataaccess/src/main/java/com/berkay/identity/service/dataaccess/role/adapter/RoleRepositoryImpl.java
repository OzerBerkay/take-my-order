package com.berkay.identity.service.dataaccess.role.adapter;

import com.berkay.identity.service.dataaccess.permission.mapper.PermissionDataAccessMapper;
import com.berkay.identity.service.dataaccess.permission.repository.PermissionJpaRepository;
import com.berkay.identity.service.dataaccess.role.entity.RoleEntity;
import com.berkay.identity.service.dataaccess.role.entity.RolePermissionEntity;
import com.berkay.identity.service.dataaccess.role.entity.RolePermissionId;
import com.berkay.identity.service.dataaccess.role.mapper.RoleDataAccessMapper;
import com.berkay.identity.service.dataaccess.role.repository.RoleJpaRepository;
import com.berkay.identity.service.dataaccess.role.repository.RolePermissionJpaRepository;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;
    private final RolePermissionJpaRepository rolePermissionJpaRepository;
    private final PermissionJpaRepository permissionJpaRepository;
    private final RoleDataAccessMapper roleDataAccessMapper;
    private final PermissionDataAccessMapper permissionDataAccessMapper;

    @Override
    @Transactional
    public Role save(Role role) {
        RoleEntity roleEntity = roleDataAccessMapper.roleToRoleEntity(role);
        RoleEntity savedEntity = roleJpaRepository.save(roleEntity);

        // Delete existing role permissions if any
        rolePermissionJpaRepository.deleteByRoleId(savedEntity.getId());

        // Insert new permissions
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            List<RolePermissionEntity> rolePermissionEntities = role.getPermissions().stream()
                    .map(permission -> RolePermissionEntity.builder()
                            .roleId(savedEntity.getId())
                            .permissionId(permission.getId().getValue())
                            .build())
                    .collect(Collectors.toList());
            rolePermissionJpaRepository.saveAll(rolePermissionEntities);
        }

        return getRoleWithPermissions(savedEntity);
    }

    @Override
    public Optional<Role> findById(RoleId roleId) {
        return roleJpaRepository.findById(roleId.getValue())
                .map(this::getRoleWithPermissions);
    }

    @Override
    public List<Role> findAllById(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return roleJpaRepository.findAllById(roleIds).stream()
                .map(this::getRoleWithPermissions)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Role role) {
        rolePermissionJpaRepository.deleteByRoleId(role.getId().getValue());
        roleJpaRepository.deleteById(role.getId().getValue());
    }

    @Override
    public List<Role> findRolesUpdatedAfter(java.time.ZonedDateTime cursor, int limit) {
        List<RoleEntity> roleEntities = roleJpaRepository.findByUpdatedAtGreaterThanOrderByUpdatedAtAsc(cursor, org.springframework.data.domain.PageRequest.of(0, limit));
        return roleEntities.stream().map(this::getRoleWithPermissions).collect(Collectors.toList());
    }

    @Override
    public boolean existsByNameAndOrganizationalUnitId(String name, UUID organizationalUnitId) {
        return roleJpaRepository.existsByNameAndOrganizationalUnitId(name, organizationalUnitId);
    }

    @Override
    public boolean existsByNameAndOrganizationalUnitIdAndIdNot(String name, UUID organizationalUnitId, UUID roleId) {
        return roleJpaRepository.existsByNameAndOrganizationalUnitIdAndIdNot(name, organizationalUnitId, roleId);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name)
                .map(this::getRoleWithPermissions);
    }

    @Override
    public Optional<Role> findByNameAndOrganizationalUnitId(String name, UUID organizationalUnitId) {
        return roleJpaRepository.findByNameAndOrganizationalUnitId(name, organizationalUnitId)
                .map(this::getRoleWithPermissions);
    }

    private Role getRoleWithPermissions(RoleEntity roleEntity) {
        List<UUID> permissionIds = rolePermissionJpaRepository.findByRoleId(roleEntity.getId()).stream()
                .map(RolePermissionEntity::getPermissionId)
                .collect(Collectors.toList());

        List<Permission> permissions = permissionJpaRepository.findAllById(permissionIds).stream()
                .map(permissionDataAccessMapper::permissionEntityToPermission)
                .collect(Collectors.toList());

        return roleDataAccessMapper.roleEntityToRole(roleEntity, permissions);
    }
}