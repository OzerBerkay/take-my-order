package com.berkay.payment.service.dataaccess.role.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "permissions_replica")
@Entity
public class PermissionReplicaEntity {
    @Id
    private UUID id;
    private String code;
    private String domain;
    private boolean isActive;
    private boolean isRestricted;
    private java.time.ZonedDateTime createdAt;
    private java.time.ZonedDateTime updatedAt;
}
