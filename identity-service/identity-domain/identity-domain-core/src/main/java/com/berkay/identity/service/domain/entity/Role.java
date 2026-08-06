package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import com.github.f4b6a3.uuid.UuidCreator;

public class Role extends AggregateRoot<RoleId> {
    private String name;
    private final UserType userType; // Update edilemez (Immutable)
    private final UUID organizationalUnitId;    // Update edilemez (Immutable)
    private final boolean isStatic;  // Statik roller update/delete edilemez
    private final UserId createdByUserId; // Hangi user oluşturdu?
    private Long version;
    private List<Permission> permissions;

    // Audit alanları
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    private Role(Builder builder) {
        super.setId(builder.roleId);
        this.name = builder.name;
        this.userType = builder.userType;
        this.organizationalUnitId = builder.organizationalUnitId;
        this.isStatic = builder.isStatic;
        this.createdByUserId = builder.createdByUserId;
        this.version = builder.version;
        this.permissions = builder.permissions;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // --- DOMAIN BEHAVIORS (İŞ KURALLARI - INITIALIZATION) ---
    public void initializeRole() {
        if (getId() == null) {
            super.setId(new RoleId(UuidCreator.getTimeOrderedEpoch()));
        }

        // Spring Data JPA requires version to be null to consider this a new entity and call persist()
        this.version = null;

        // İlk yaratılışta zaman damgalarını Domain seviyesinde set et
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        this.createdAt = now;
        this.updatedAt = now;

        validateRoleInvariants();
    }

    // --- DOMAIN BEHAVIORS (UPDATE) ---
    public void updateRole(String newName, List<Permission> newPermissions) {
        if (this.isStatic) {
            throw new IdentityDomainException("Static roles (is_static=true) cannot be updated!");
        }

        this.name = newName;
        this.permissions = newPermissions;
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC")); // Update anında zamanı güncelle

        validateRoleInvariants();
    }

    public void validateDelete() {
        if (this.isStatic) {
            throw new IdentityDomainException("Static roles (is_static=true) cannot be deleted!");
        }
    }

    private void validateRoleInvariants() {
        if (name == null || name.trim().isEmpty()) {
            throw new IdentityDomainException("Role name cannot be empty!");
        }
        if (permissions == null || permissions.isEmpty()) {
            throw new IdentityDomainException("Role must contain at least 1 permission!");
        }

        if (UserType.CUSTOMER.equals(userType) && organizationalUnitId != null) {
            throw new IdentityDomainException("For CUSTOMER role, organizationalUnitId must be NULL!");
        }
        if (UserType.INTERNAL.equals(userType) && organizationalUnitId != null) {
            throw new IdentityDomainException("For INTERNAL role, organizationalUnitId must be NULL!");
        }
        if (UserType.MERCHANT.equals(userType) && organizationalUnitId == null && !this.isStatic) {
            throw new IdentityDomainException("For dynamic MERCHANT roles, organizationalUnitId (Restaurant ID) cannot be NULL!");
        }

        boolean hasInactivePermission = permissions.stream().anyMatch(p -> !p.isActive());
        if (hasInactivePermission) {
            throw new IdentityDomainException("Cannot assign inactive permissions to a role!");
        }

        boolean hasRestrictedPermission = permissions.stream().anyMatch(Permission::isRestricted);
        if (hasRestrictedPermission && !this.isStatic) {
            throw new IdentityDomainException("Cannot assign restricted permissions to any role manually!");
        }
    }

    // --- GETTERS ---
    public String getName() { return name; }
    public UserType getUserType() { return userType; }
    public UUID getOrganizationalUnitId() { return organizationalUnitId; }
    public boolean isStatic() { return isStatic; }
    public UserId getCreatedByUserId() { return createdByUserId; }
    public Long getVersion() { return version; }
    public List<Permission> getPermissions() { return permissions; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private RoleId roleId;
        private String name;
        private UserType userType;
        private UUID organizationalUnitId;
        private boolean isStatic;
        private UserId createdByUserId;
        private Long version;
        private List<Permission> permissions;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public Builder roleId(RoleId roleId) { this.roleId = roleId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder userType(UserType userType) { this.userType = userType; return this; }
        public Builder organizationalUnitId(UUID organizationalUnitId) { this.organizationalUnitId = organizationalUnitId; return this; }
        public Builder isStatic(boolean isStatic) { this.isStatic = isStatic; return this; }
        public Builder createdByUserId(UserId createdByUserId) { this.createdByUserId = createdByUserId; return this; }
        public Builder version(Long version) { this.version = version; return this; }
        public Builder permissions(List<Permission> permissions) { this.permissions = permissions; return this; }
        public Builder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Role build() { return new Role(this); }
    }
}