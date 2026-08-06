package com.berkay.order.service.dataaccess.role.repository;

import com.berkay.order.service.dataaccess.role.entity.PermissionReplicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PermissionReplicaRepository extends JpaRepository<PermissionReplicaEntity, UUID> {
}
