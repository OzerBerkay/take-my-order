package com.berkay.identity.service.dataaccess.permission.repository;

import com.berkay.identity.service.dataaccess.permission.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, UUID>, JpaSpecificationExecutor<PermissionEntity> {
    List<PermissionEntity> findByIdInAndActiveTrue(Set<UUID> ids);
    
    @Query("SELECT p FROM PermissionEntity p WHERE p.id IN (SELECT rp.permissionId FROM RolePermissionEntity rp WHERE rp.roleId IN :roleIds) AND p.active = true")
    List<PermissionEntity> findActivePermissionsByRoleIds(@Param("roleIds") List<UUID> roleIds);

    boolean existsByCode(String code);

    Optional<PermissionEntity> findByCode(String code);
}