package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.domain.entity.Permission;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PermissionRepository {
    // Verilen ID'lere sahip olan ve is_active = true olan yetkileri getirir
    List<Permission> findActivePermissionsByIds(Set<UUID> permissionIds);

    // Verilen Rol ID'lerine sahip olan aktif yetkileri getirir
    List<Permission> findActivePermissionsByRoleIds(List<UUID> roleIds);

    Permission save(Permission permission);
    
    java.util.Optional<Permission> findById(com.berkay.identity.service.domain.valueobject.PermissionId permissionId);
    
    boolean existsByCode(String code);

    java.util.Optional<Permission> findByCode(String code);
    
    void delete(Permission permission);
}