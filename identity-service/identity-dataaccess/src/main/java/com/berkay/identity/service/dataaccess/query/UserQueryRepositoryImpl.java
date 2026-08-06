package com.berkay.identity.service.dataaccess.query;

import com.berkay.identity.service.dataaccess.user.entity.UserEntity;
import com.berkay.identity.service.dataaccess.user.entity.UserRoleEntity;
import com.berkay.identity.service.dataaccess.user.repository.UserJpaRepository;
import com.berkay.identity.service.dataaccess.user.repository.UserRoleJpaRepository;
import com.berkay.identity.service.dataaccess.user.repository.UserSpecification;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.dto.query.*;
import com.berkay.identity.service.mapper.RoleDataMapper;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.repository.UserQueryRepository;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserRoleJpaRepository userRoleJpaRepository;
    private final RoleRepository roleRepository;
    private final RoleDataMapper roleDataMapper;

    @Override
    public PageResult<UserResponse> getAdminUsers(GetAdminUsersQuery query) {
        Specification<UserEntity> spec = Specification.where(null);

        if (query.getEmail() != null) spec = spec.and(UserSpecification.hasEmail(query.getEmail()));
        if (query.getFirstName() != null) spec = spec.and(UserSpecification.hasFirstName(query.getFirstName()));
        if (query.getLastName() != null) spec = spec.and(UserSpecification.hasLastName(query.getLastName()));
        if (query.getStatus() != null) spec = spec.and(UserSpecification.hasStatus(query.getStatus()));
        if (query.getUserType() != null) spec = spec.and(UserSpecification.hasUserType(query.getUserType()));
        if (query.getOrgUnitId() != null) spec = spec.and(UserSpecification.hasOrgUnitId(query.getOrgUnitId()));
        if (query.getRoleId() != null) spec = spec.and(UserSpecification.hasRole(query.getRoleId()));

        Page<UserEntity> pageResult = userJpaRepository.findAll(spec, PageRequest.of(query.getPage(), query.getSize()));
        Map<UUID, List<RoleResponse>> userRolesMap = fetchRolesForUsers(pageResult.getContent());

        return PageResult.<UserResponse>builder()
                .data(pageResult.getContent().stream()
                        .map(user -> mapUserToResponse(user, userRolesMap.getOrDefault(user.getId(), new ArrayList<>())))
                        .collect(Collectors.toList()))
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

    @Override
    public PageResult<MerchantUserResponse> getMerchantUsers(GetMerchantUsersQuery query) {
        Specification<UserEntity> spec = Specification.where(null);

        if (query.getEmail() != null) spec = spec.and(UserSpecification.hasEmail(query.getEmail()));
        if (query.getFirstName() != null) spec = spec.and(UserSpecification.hasFirstName(query.getFirstName()));
        if (query.getLastName() != null) spec = spec.and(UserSpecification.hasLastName(query.getLastName()));

        List<UUID> authorizedOrgUnits = query.getAuthorizedOrgUnitIds();
        
        if (query.getFilterOrgUnitId() == null) {
            throw new IdentityDomainException("orgUnitId is required for merchant queries.");
        }

        if (!authorizedOrgUnits.contains(query.getFilterOrgUnitId())) {
            throw new IdentityDomainException("Unauthorized organizational unit access.");
        }
        
        spec = spec.and(UserSpecification.hasOrgUnitId(query.getFilterOrgUnitId()));
        if (query.getRoleId() != null) spec = spec.and(UserSpecification.hasRole(query.getRoleId()));

        Page<UserEntity> pageResult = userJpaRepository.findAll(spec, PageRequest.of(query.getPage(), query.getSize()));
        Map<UUID, List<RoleResponse>> userRolesMap = fetchRolesForUsers(pageResult.getContent());

        return PageResult.<MerchantUserResponse>builder()
                .data(pageResult.getContent().stream()
                        .map(user -> mapMerchantUserToResponse(user, query.getFilterOrgUnitId(), userRolesMap.getOrDefault(user.getId(), new ArrayList<>())))
                        .collect(Collectors.toList()))
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

    @Override
    public UserResponse getAdminUserById(UUID userId) {
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Map<UUID, List<RoleResponse>> userRolesMap = fetchRolesForUsers(Collections.singletonList(user));
        return mapUserToResponse(user, userRolesMap.getOrDefault(user.getId(), new ArrayList<>()));
    }

    @Override
    public MerchantUserResponse getMerchantUserById(UUID userId, UUID orgUnitId, List<UUID> authorizedOrgUnitIds) {
        if (!authorizedOrgUnitIds.contains(orgUnitId)) {
            throw new IdentityDomainException("Unauthorized organizational unit access.");
        }

        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        boolean isAssociated = user.getOrganizationalUnitIds().contains(orgUnitId);

        if (!isAssociated) {
            throw new IdentityDomainException("User is not associated with this organizational unit.");
        }

        Map<UUID, List<RoleResponse>> userRolesMap = fetchRolesForUsers(Collections.singletonList(user));
        return mapMerchantUserToResponse(user, orgUnitId, userRolesMap.getOrDefault(user.getId(), new ArrayList<>()));
    }

    private Map<UUID, List<RoleResponse>> fetchRolesForUsers(List<UserEntity> users) {
        if (users.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UUID> userIds = users.stream().map(UserEntity::getId).collect(Collectors.toList());
        List<UserRoleEntity> userRoleEntities = userRoleJpaRepository.findByUserIdIn(userIds);
        
        if (userRoleEntities.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UUID> roleIds = userRoleEntities.stream()
                .map(UserRoleEntity::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        List<Role> roles = roleRepository.findAllById(roleIds);
        Map<UUID, RoleResponse> roleMap = roles.stream()
                .collect(Collectors.toMap(
                        role -> role.getId().getValue(),
                        roleDataMapper::roleToRoleResponse
                ));

        Map<UUID, List<RoleResponse>> userRolesMap = new HashMap<>();
        for (UserRoleEntity ur : userRoleEntities) {
            RoleResponse roleResponse = roleMap.get(ur.getRoleId());
            if (roleResponse != null) {
                userRolesMap.computeIfAbsent(ur.getUserId(), k -> new ArrayList<>()).add(roleResponse);
            }
        }

        return userRolesMap;
    }

    private UserResponse mapUserToResponse(UserEntity user, List<RoleResponse> roles) {
        return UserResponse.builder()
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
                .roles(roles)
                .build();
    }
    
    private MerchantUserResponse mapMerchantUserToResponse(UserEntity user, UUID orgUnitId, List<RoleResponse> roles) {
        List<RoleResponse> filteredRoles = roles.stream()
                .filter(role -> orgUnitId.equals(role.getOrganizationalUnitId()))
                .collect(Collectors.toList());
                
        return MerchantUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .userType(user.getUserType())
                .status(user.getStatus() == com.berkay.identity.service.domain.valueobject.AccountStatus.ACTIVE)
                .organizationalUnitId(orgUnitId) // Sadece 1 tane ve o restoranın id'si!
                .roles(filteredRoles) // Sadece o restorandaki yetkileri!
                .build();
    }
}
