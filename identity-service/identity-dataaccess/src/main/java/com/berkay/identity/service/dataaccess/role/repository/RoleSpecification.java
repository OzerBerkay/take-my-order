package com.berkay.identity.service.dataaccess.role.repository;

import com.berkay.identity.service.dataaccess.role.entity.RoleEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public class RoleSpecification {

    public static Specification<RoleEntity> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<RoleEntity> hasOrgUnitId(UUID orgUnitId) {
        return (root, query, cb) -> {
            if (orgUnitId == null) return null;
            return cb.equal(root.get("organizationalUnitId"), orgUnitId);
        };
    }

    public static Specification<RoleEntity> hasAnyOrgUnitId(List<UUID> orgUnitIds) {
        return (root, query, cb) -> {
            if (orgUnitIds == null || orgUnitIds.isEmpty()) return cb.disjunction(); // Block access instead of bypassing filter
            return root.get("organizationalUnitId").in(orgUnitIds);
        };
    }

    public static Specification<RoleEntity> isGlobal() {
        return (root, query, cb) -> cb.isNull(root.get("organizationalUnitId"));
    }

    public static Specification<RoleEntity> hasUserType(String userType) {
        return (root, query, cb) -> {
            if (userType == null || userType.isBlank()) return null;
            return cb.equal(root.get("userType"), com.berkay.identity.service.domain.valueobject.UserType.valueOf(userType.toUpperCase()));
        };
    }
}
