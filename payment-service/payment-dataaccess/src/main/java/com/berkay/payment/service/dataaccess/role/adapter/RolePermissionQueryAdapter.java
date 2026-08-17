package com.berkay.payment.service.dataaccess.role.adapter;

import com.berkay.payment.service.dataaccess.role.entity.PermissionReplicaEntity;
import com.berkay.payment.service.dataaccess.role.entity.RolePermissionReplicaEntity;
import com.berkay.payment.service.dataaccess.role.repository.PermissionReplicaRepository;
import com.berkay.payment.service.dataaccess.role.repository.RolePermissionReplicaRepository;
import com.berkay.payment.service.dataaccess.role.entity.RoleReplicaEntity;
import com.berkay.payment.service.dataaccess.role.repository.RoleReplicaRepository;
import com.berkay.payment.service.domain.ports.output.repository.RolePermissionQueryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RolePermissionQueryAdapter implements RolePermissionQueryPort {

    private final RolePermissionReplicaRepository rolePermissionReplicaRepository;
    private final PermissionReplicaRepository permissionReplicaRepository;
    private final RoleReplicaRepository roleReplicaRepository;

    public RolePermissionQueryAdapter(RolePermissionReplicaRepository rolePermissionReplicaRepository,
                                      PermissionReplicaRepository permissionReplicaRepository,
                                      RoleReplicaRepository roleReplicaRepository) {
        this.rolePermissionReplicaRepository = rolePermissionReplicaRepository;
        this.permissionReplicaRepository = permissionReplicaRepository;
        this.roleReplicaRepository = roleReplicaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#roleId")
    public Set<String> getPermissionCodesByRoleId(UUID roleId) {
        List<RolePermissionReplicaEntity> rolePermissions = rolePermissionReplicaRepository.findByRoleId(roleId);
        if (rolePermissions.isEmpty()) {
            return Set.of();
        }

        Set<UUID> permissionIds = rolePermissions.stream()
                .map(RolePermissionReplicaEntity::getPermissionId)
                .collect(Collectors.toSet());

        List<PermissionReplicaEntity> permissions = permissionReplicaRepository.findAllById(permissionIds);

        return permissions.stream()
                .filter(PermissionReplicaEntity::isActive)
                .map(PermissionReplicaEntity::getCode)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "role_org_units", key = "#roleId")
    public Optional<UUID> getOrganizationalUnitIdByRoleId(UUID roleId) {
        return roleReplicaRepository.findById(roleId)
                .map(RoleReplicaEntity::getOrganizationalUnitId);
    }
}
