package com.berkay.identity.service.dataaccess.role.repository;

import com.berkay.identity.service.dataaccess.role.entity.RolePermissionEntity;
import com.berkay.identity.service.dataaccess.role.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionEntity, RolePermissionId> {
    List<RolePermissionEntity> findByRoleId(UUID roleId);
    void deleteByRoleId(UUID roleId);
}
