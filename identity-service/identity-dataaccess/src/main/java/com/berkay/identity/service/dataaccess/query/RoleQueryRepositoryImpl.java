package com.berkay.identity.service.dataaccess.query;

import com.berkay.identity.service.dataaccess.role.entity.RoleEntity;
import com.berkay.identity.service.dataaccess.role.repository.RoleJpaRepository;
import com.berkay.identity.service.dataaccess.role.repository.RoleSpecification;
import com.berkay.identity.service.dataaccess.role.entity.RolePermissionEntity;
import com.berkay.identity.service.dataaccess.role.repository.RolePermissionJpaRepository;
import com.berkay.identity.service.dataaccess.permission.entity.PermissionEntity;
import com.berkay.identity.service.dataaccess.permission.repository.PermissionJpaRepository;
import com.berkay.identity.service.dto.query.PageResult;
import com.berkay.identity.service.dto.query.RoleResponse;
import com.berkay.identity.service.dto.query.UserResponse;
import com.berkay.identity.service.dto.query.PermissionResponse;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.ports.output.repository.RoleQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleQueryRepositoryImpl implements RoleQueryRepository {

    private final RoleJpaRepository roleJpaRepository;
    private final RolePermissionJpaRepository rolePermissionJpaRepository;
    private final PermissionJpaRepository permissionJpaRepository;
    private final com.berkay.identity.service.dataaccess.user.repository.UserJpaRepository userJpaRepository;

    @Override
    public PageResult<RoleResponse> getAdminRoles(int page, int size, String name, UUID orgUnitId, String userType) {
        Specification<RoleEntity> spec = Specification.where(null);
        if (name != null) spec = spec.and(RoleSpecification.hasName(name));
        
        if (orgUnitId != null) {
            spec = spec.and(RoleSpecification.hasOrgUnitId(orgUnitId));
        }

        if (userType != null) {
            spec = spec.and(RoleSpecification.hasUserType(userType));
        }

        Page<RoleEntity> pageResult = roleJpaRepository.findAll(spec, PageRequest.of(page, size));

        return PageResult.<RoleResponse>builder()
                .data(pageResult.getContent().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

    @Override
    public PageResult<RoleResponse> getMerchantRoles(int page, int size, String name, UUID orgUnitId, List<UUID> authorizedOrgUnitIds) {
        Specification<RoleEntity> spec = Specification.where(null);
        if (name != null) spec = spec.and(RoleSpecification.hasName(name));

        if (orgUnitId == null || authorizedOrgUnitIds == null || !authorizedOrgUnitIds.contains(orgUnitId)) {
            spec = spec.and((root, query, cb) -> cb.disjunction());
        } else {
            spec = spec.and(RoleSpecification.hasOrgUnitId(orgUnitId));
        }

        Page<RoleEntity> pageResult = roleJpaRepository.findAll(spec, PageRequest.of(page, size));

        return PageResult.<RoleResponse>builder()
                .data(pageResult.getContent().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

    @Override
    public RoleResponse getRoleById(UUID roleId, List<UUID> authorizedOrgUnitIds) {
        RoleEntity role = roleJpaRepository.findById(roleId)
                .orElseThrow(() -> new IdentityDomainException("Role not found with id: " + roleId));

        if (authorizedOrgUnitIds != null) {
            if (role.getOrganizationalUnitId() == null || !authorizedOrgUnitIds.contains(role.getOrganizationalUnitId())) {
                throw new IdentityDomainException("Unauthorized access to role details.");
            }
        }

        return mapToResponse(role);
    }

    private RoleResponse mapToResponse(RoleEntity role) {
        List<UUID> permissionIds = rolePermissionJpaRepository.findByRoleId(role.getId()).stream()
                .map(RolePermissionEntity::getPermissionId)
                .collect(Collectors.toList());

        List<PermissionResponse> permissions = permissionJpaRepository.findAllById(permissionIds).stream()
                .map(p -> PermissionResponse.builder()
                        .id(p.getId())
                        .name(p.getCode())
                        .description(p.getDescription())
                        .active(p.isActive())
                        .isRestricted(p.isRestricted())
                        .build())
                .collect(Collectors.toList());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .isStatic(role.isStatic())
                .organizationalUnitId(role.getOrganizationalUnitId())
                .userType(role.getUserType().name())
                .createdAt(role.getCreatedAt())
                .permissions(permissions)
                .build();
    }

    private com.berkay.identity.service.dto.query.UserResponse mapUserToResponse(com.berkay.identity.service.dataaccess.user.entity.UserEntity user, List<UUID> allowedOrgUnitIds) {
        return com.berkay.identity.service.dto.query.UserResponse.builder()
                .id(user.getId())
                .externalId(user.getExternalId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .userType(user.getUserType())
                .status(user.getStatus())
                .authProvider(user.getAuthProvider())
                .isEmailVerified(user.getIsEmailVerified() != null ? user.getIsEmailVerified() : false)
                .isPhoneVerified(user.getIsPhoneVerified() != null ? user.getIsPhoneVerified() : false)
                .imageUrl(user.getImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .organizationalUnitIds(user.getOrganizationalUnitIds())
                // We leave roles empty for this specific list response because querying them per user in a loop
                // would cause N+1. Alternatively, you could fetch them, but for the basic list it's often omitted.
                .roles(new java.util.ArrayList<>())
                .build();
    }
}
