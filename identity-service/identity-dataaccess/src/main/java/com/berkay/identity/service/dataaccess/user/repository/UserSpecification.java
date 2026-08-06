package com.berkay.identity.service.dataaccess.user.repository;

import com.berkay.identity.service.dataaccess.role.entity.RoleEntity;
import com.berkay.identity.service.dataaccess.user.entity.UserEntity;
import com.berkay.identity.service.domain.valueobject.UserType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public class UserSpecification {

    public static Specification<UserEntity> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<UserEntity> hasFirstName(String firstName) {
        return (root, query, cb) -> {
            if (firstName == null || firstName.isBlank()) return null;
            return cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%");
        };
    }

    public static Specification<UserEntity> hasLastName(String lastName) {
        return (root, query, cb) -> {
            if (lastName == null || lastName.isBlank()) return null;
            return cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
        };
    }

    public static Specification<UserEntity> hasStatus(com.berkay.identity.service.domain.valueobject.AccountStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<UserEntity> hasUserType(UserType userType) {
        return (root, query, cb) -> {
            if (userType == null) return null;
            return cb.equal(root.get("userType"), userType);
        };
    }

    public static Specification<UserEntity> hasAnyOrgUnitId(List<UUID> orgUnitIds) {
        return (root, query, cb) -> {
            if (orgUnitIds == null || orgUnitIds.isEmpty()) return cb.disjunction(); // Block access instead of bypassing filter
            // Join the basic element collection organizationalUnitIds
            Join<UserEntity, UUID> join = root.join("organizationalUnitIds");
            return join.in(orgUnitIds);
        };
    }

    public static Specification<UserEntity> hasOrgUnitId(UUID orgUnitId) {
        return (root, query, cb) -> {
            if (orgUnitId == null) return null;
            Join<UserEntity, UUID> join = root.join("organizationalUnitIds");
            return cb.equal(join, orgUnitId);
        };
    }

    public static Specification<UserEntity> hasRole(UUID roleId) {
        return (root, query, cb) -> {
            if (roleId == null) return null;
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<com.berkay.identity.service.dataaccess.user.entity.UserRoleEntity> subRoot = subquery.from(com.berkay.identity.service.dataaccess.user.entity.UserRoleEntity.class);
            subquery.select(subRoot.get("userId"))
                    .where(cb.equal(subRoot.get("roleId"), roleId));
            return root.get("id").in(subquery);
        };
    }
}
