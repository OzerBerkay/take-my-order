package com.berkay.order.service.dataaccess.role.adapter;

import com.berkay.order.service.dataaccess.role.entity.PermissionReplicaEntity;
import com.berkay.order.service.dataaccess.role.entity.RolePermissionReplicaEntity;
import com.berkay.order.service.dataaccess.role.entity.RoleReplicaEntity;
import com.berkay.order.service.dataaccess.role.repository.PermissionReplicaRepository;
import com.berkay.order.service.dataaccess.role.repository.RolePermissionReplicaRepository;
import com.berkay.order.service.dataaccess.role.repository.RoleReplicaRepository;
import com.berkay.order.service.domain.dto.message.PermissionEventPayload;
import com.berkay.order.service.domain.dto.message.RoleEventPayload;
import com.berkay.order.service.domain.ports.output.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleReplicaRepository roleReplicaRepository;
    private final PermissionReplicaRepository permissionReplicaRepository;
    private final RolePermissionReplicaRepository rolePermissionReplicaRepository;

    public RoleRepositoryImpl(RoleReplicaRepository roleReplicaRepository,
                              PermissionReplicaRepository permissionReplicaRepository,
                              RolePermissionReplicaRepository rolePermissionReplicaRepository) {
        this.roleReplicaRepository = roleReplicaRepository;
        this.permissionReplicaRepository = permissionReplicaRepository;
        this.rolePermissionReplicaRepository = rolePermissionReplicaRepository;
    }

    @Override
    public long count() {
        return roleReplicaRepository.count();
    }

    @Override
    @Transactional
    public void deleteAll() {
        rolePermissionReplicaRepository.deleteAllInBatch();
        permissionReplicaRepository.deleteAllInBatch();
        roleReplicaRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void save(RoleEventPayload payload) {
        // 1. Versiyon Kontrolü ve Rolü Kaydet (Upsert)
        java.util.Optional<RoleReplicaEntity> existingRoleOpt = roleReplicaRepository.findById(payload.getRoleId());
        if (existingRoleOpt.isPresent() && existingRoleOpt.get().getVersion() != null && payload.getVersion() != null) {
            if (payload.getVersion() <= existingRoleOpt.get().getVersion()) {
                log.info("Ignoring role sync for id: {} because incoming version {} is <= existing version {}",
                        payload.getRoleId(), payload.getVersion(), existingRoleOpt.get().getVersion());
                return;
            }
        }

        RoleReplicaEntity entity = RoleReplicaEntity.builder()
                .id(payload.getRoleId())
                .name(payload.getName())
                .userType(payload.getUserType())
                .organizationalUnitId(payload.getOrganizationalUnitId())
                .version(payload.getVersion())
                .build();
        roleReplicaRepository.save(entity);


        // 2. Yetkileri (Permissions) filtrele ve Bulk Insert yap (Sıfır N+1 Kuralı)
        if (payload.getPermissions() != null) {
            List<com.berkay.order.service.domain.dto.message.PermissionPayload> filteredPermissions = payload.getPermissions().stream()
                    .filter(p -> "ORDER".equalsIgnoreCase(p.getDomain()))
                    .toList();

            Set<UUID> incomingPermissionIds = filteredPermissions.stream()
                    .map(p -> p.getId())
                    .collect(Collectors.toSet());

            if (!filteredPermissions.isEmpty()) {
                // DB'de var olan yetkilerin ID'lerini bulk fetch et
                List<PermissionReplicaEntity> existingPermissions = permissionReplicaRepository.findAllById(incomingPermissionIds);
                Set<UUID> existingPermissionIds = existingPermissions.stream()
                        .map(PermissionReplicaEntity::getId)
                        .collect(Collectors.toSet());

                // RAM üzerinde Set Difference işlemi ile "yepyeni" yetkileri bul
                List<PermissionReplicaEntity> newPermissionsToInsert = filteredPermissions.stream()
                        .filter(p -> !existingPermissionIds.contains(p.getId()))
                        .map(p -> PermissionReplicaEntity.builder()
                                .id(p.getId())
                                .code(p.getCode())
                                .domain(p.getDomain())
                                .isActive(p.getIsActive() != null ? p.getIsActive() : true)
                                .isRestricted(p.getIsRestricted() != null ? p.getIsRestricted() : false)
                                .createdAt(p.getCreatedAt() != null ? java.time.ZonedDateTime.parse(p.getCreatedAt()) : java.time.ZonedDateTime.now())
                                .updatedAt(p.getUpdatedAt() != null ? java.time.ZonedDateTime.parse(p.getUpdatedAt()) : java.time.ZonedDateTime.now())
                                .build())
                        .toList();

                if (!newPermissionsToInsert.isEmpty()) {
                    permissionReplicaRepository.saveAll(newPermissionsToInsert);
                }
            }

            // 3. Ara Tabloyu (Junction) Yenile (Replace-All)
            rolePermissionReplicaRepository.deleteByRoleId(payload.getRoleId());
            if (!incomingPermissionIds.isEmpty()) {
                List<RolePermissionReplicaEntity> newRolePermissions = incomingPermissionIds.stream()
                        .map(permId -> RolePermissionReplicaEntity.builder()
                                .roleId(payload.getRoleId())
                                .permissionId(permId)
                                .build())
                        .toList();
                rolePermissionReplicaRepository.saveAll(newRolePermissions);
            }
        }
    }

    @Override
    @Transactional
    public void delete(UUID roleId) {
        rolePermissionReplicaRepository.deleteByRoleId(roleId);
        roleReplicaRepository.deleteById(roleId);
    }

    @Override
    @Transactional
    public void updatePermission(PermissionEventPayload payload) {
        log.info("Updating permission id: {}", payload.getPermission().getId());
        com.berkay.order.service.domain.dto.message.PermissionPayload permissionPayload = payload.getPermission();
        permissionReplicaRepository.findById(permissionPayload.getId()).ifPresent(permissionReplicaEntity -> {
            if (permissionPayload.getIsActive() != null) {
                permissionReplicaEntity.setActive(permissionPayload.getIsActive());
            }
            permissionReplicaRepository.save(permissionReplicaEntity);
        });
    }
}
