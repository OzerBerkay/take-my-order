package com.berkay.payment.service.dataaccess.role.repository;

import com.berkay.payment.service.dataaccess.role.entity.RolePermissionId;
import com.berkay.payment.service.dataaccess.role.entity.RolePermissionReplicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RolePermissionReplicaRepository extends JpaRepository<RolePermissionReplicaEntity, RolePermissionId> {
    void deleteByRoleId(UUID roleId);
    java.util.List<RolePermissionReplicaEntity> findByRoleId(UUID roleId);
}
