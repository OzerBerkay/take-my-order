package com.berkay.restaurant.service.dataaccess.role.entity;

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
@Table(name = "roles_replica")
@Entity
public class RoleReplicaEntity {
    
    @Id
    private UUID id;
    private String name;
    private String userType;
    private UUID organizationalUnitId;
    private Long version;
}
