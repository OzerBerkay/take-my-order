package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.identity.service.domain.valueobject.DomainType;
import com.berkay.identity.service.domain.valueobject.PermissionId;

import java.time.ZonedDateTime;

public class Permission extends BaseEntity<PermissionId> {
    private String code;
    private String description;
    private DomainType domain;
    private boolean active;
    private boolean isRestricted;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    private Permission(Builder builder) {
        super.setId(builder.permissionId);
        this.code = builder.code;
        this.description = builder.description;
        this.domain = builder.domain;
        this.active = builder.active;
        this.isRestricted = builder.isRestricted;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public void initializePermission() {
        setId(new PermissionId(java.util.UUID.randomUUID()));
        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneId.of("UTC"));
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void updatePermission(String description, boolean active) {
        this.description = description;
        this.active = active;
        this.updatedAt = ZonedDateTime.now(java.time.ZoneId.of("UTC"));
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
    public DomainType getDomain() { return domain; }
    public boolean isActive() { return active; }
    public boolean isRestricted() { return isRestricted; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private PermissionId permissionId;
        private String code;
        private String description;
        private DomainType domain;
        private boolean active = true;
        private boolean isRestricted = false;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public Builder permissionId(PermissionId permissionId) { this.permissionId = permissionId; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder domain(DomainType domain) { this.domain = domain; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder isRestricted(boolean isRestricted) { this.isRestricted = isRestricted; return this; }
        public Builder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Permission build() { return new Permission(this); }
    }
}