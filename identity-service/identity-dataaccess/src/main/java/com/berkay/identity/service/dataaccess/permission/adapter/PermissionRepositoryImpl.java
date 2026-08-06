package com.berkay.identity.service.dataaccess.permission.adapter;

import com.berkay.identity.service.dataaccess.permission.mapper.PermissionDataAccessMapper;
import com.berkay.identity.service.dataaccess.permission.repository.PermissionJpaRepository;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.ports.output.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionJpaRepository permissionJpaRepository;
    private final PermissionDataAccessMapper permissionDataAccessMapper;

    @Override
    public List<Permission> findActivePermissionsByIds(Set<UUID> permissionIds) {
        return permissionDataAccessMapper.permissionEntityListToPermissionList(
                permissionJpaRepository.findByIdInAndActiveTrue(permissionIds)
        );
    }

    @Override
    public List<Permission> findActivePermissionsByRoleIds(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return permissionJpaRepository.findActivePermissionsByRoleIds(roleIds)
                .stream()
                .map(permissionDataAccessMapper::permissionEntityToPermission)
                .toList();
    }

    @Override
    public Permission save(Permission permission) {
        return permissionDataAccessMapper.permissionEntityToPermission(
                permissionJpaRepository.save(permissionDataAccessMapper.permissionToPermissionEntity(permission))
        );
    }

    @Override
    public java.util.Optional<Permission> findById(com.berkay.identity.service.domain.valueobject.PermissionId permissionId) {
        return permissionJpaRepository.findById(permissionId.getValue())
                .map(permissionDataAccessMapper::permissionEntityToPermission);
    }

    @Override
    public boolean existsByCode(String code) {
        return permissionJpaRepository.existsByCode(code);
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        return permissionJpaRepository.findByCode(code)
                .map(permissionDataAccessMapper::permissionEntityToPermission);
    }

    @Override
    public void delete(Permission permission) {
        permissionJpaRepository.deleteById(permission.getId().getValue());
    }
}