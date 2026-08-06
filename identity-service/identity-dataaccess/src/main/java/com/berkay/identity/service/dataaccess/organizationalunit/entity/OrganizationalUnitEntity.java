package com.berkay.identity.service.dataaccess.organizationalunit.entity;

import com.berkay.identity.service.domain.valueobject.OrganizationalUnitType;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organizational_units_replica")
@Entity
public class OrganizationalUnitEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationalUnitType type;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now(java.time.ZoneId.of("UTC"));
        }
        if (updatedAt == null) {
            updatedAt = ZonedDateTime.now(java.time.ZoneId.of("UTC"));
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = ZonedDateTime.now(java.time.ZoneId.of("UTC"));
    }
}
