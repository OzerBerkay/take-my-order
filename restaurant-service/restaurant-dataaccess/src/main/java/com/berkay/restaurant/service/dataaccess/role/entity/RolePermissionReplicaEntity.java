package com.berkay.restaurant.service.dataaccess.role.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RolePermissionId.class)
@Table(name = "role_permissions_replica")
@Entity
public class RolePermissionReplicaEntity {
    @Id
    private UUID roleId;
    @Id
    private UUID permissionId;
}
