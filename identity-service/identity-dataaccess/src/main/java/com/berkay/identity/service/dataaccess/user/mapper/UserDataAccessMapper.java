package com.berkay.identity.service.dataaccess.user.mapper;

import com.berkay.identity.service.dataaccess.permission.entity.PermissionEntity;
import com.berkay.identity.service.dataaccess.role.entity.RoleEntity;
import com.berkay.identity.service.dataaccess.user.entity.UserEntity;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDataAccessMapper {

    public UserEntity userToUserEntity(User user) {
        UserEntity userEntity = UserEntity.builder()
                .id(user.getId().getValue())
                .externalId(user.getExternalId())
                .authProvider(user.getAuthProvider())
                .email(user.getEmail().getValue())
                .phoneNumber(user.getPhoneNumber().getValue())
                .firstName(user.getFirstName().getValue())
                .lastName(user.getLastName().getValue())
                .imageUrl(user.getImageUrl())
                .isEmailVerified(user.isEmailVerified())
                .isPhoneVerified(user.isPhoneVerified())
                .userType(user.getUserType())
                .status(user.getStatus())
                .organizationalUnitIds(user.getOrganizationalUnitIds())
                .build();

        userEntity.setCreatedAt(user.getCreatedAt());
        userEntity.setUpdatedAt(user.getUpdatedAt());

        return userEntity;
    }

    public User userEntityToUser(UserEntity userEntity) {
        return User.builder()
                .userId(new UserId(userEntity.getId()))
                .externalId(userEntity.getExternalId())
                .authProvider(userEntity.getAuthProvider())
                .email(new UserEmail(userEntity.getEmail()))
                .phoneNumber(new PhoneNumber(userEntity.getPhoneNumber()))
                .firstName(new FirstName(userEntity.getFirstName()))
                .lastName(new LastName(userEntity.getLastName()))
                .imageUrl(userEntity.getImageUrl())
                .isEmailVerified(userEntity.getIsEmailVerified())
                .isPhoneVerified(userEntity.getIsPhoneVerified())
                .userType(userEntity.getUserType())
                .status(userEntity.getStatus())
                .organizationalUnitIds(userEntity.getOrganizationalUnitIds())
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .build();
    }

    public User userEntityToUserWithCollections(UserEntity userEntity, List<Object> dummyAddresses, List<Role> roles) {
        return User.builder()
                .userId(new UserId(userEntity.getId()))
                .externalId(userEntity.getExternalId())
                .authProvider(userEntity.getAuthProvider())
                .email(new UserEmail(userEntity.getEmail()))
                .phoneNumber(new PhoneNumber(userEntity.getPhoneNumber()))
                .firstName(new FirstName(userEntity.getFirstName()))
                .lastName(new LastName(userEntity.getLastName()))
                .imageUrl(userEntity.getImageUrl())
                .isEmailVerified(userEntity.getIsEmailVerified())
                .isPhoneVerified(userEntity.getIsPhoneVerified())
                .userType(userEntity.getUserType())
                .status(userEntity.getStatus())
                .roles(roles)
                .organizationalUnitIds(userEntity.getOrganizationalUnitIds())
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .build();
    }

    public List<RoleEntity> roleListToRoleEntityList(List<Role> roles) {
        if (roles == null) return new ArrayList<>();
        return roles.stream()
                .map(role -> RoleEntity.builder()
                        .id(role.getId().getValue())
                        .name(role.getName())
                        .userType(role.getUserType())
                        .organizationalUnitId(role.getOrganizationalUnitId())
                        .isStatic(role.isStatic())
                        .createdByUserId(role.getCreatedByUserId() != null ? role.getCreatedByUserId().getValue() : null)
                        .version(role.getVersion())
                        .createdAt(role.getCreatedAt())
                        .updatedAt(role.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public Role roleEntityToRoleWithPermissions(RoleEntity entity, List<PermissionEntity> permissionEntities) {
        if (entity == null) return null;
        return Role.builder()
                .roleId(new RoleId(entity.getId()))
                .name(entity.getName())
                .userType(entity.getUserType())
                .organizationalUnitId(entity.getOrganizationalUnitId())
                .isStatic(entity.isStatic())
                .createdByUserId(entity.getCreatedByUserId() != null ? new UserId(entity.getCreatedByUserId()) : null)
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .permissions(permissionEntityListToPermissionList(permissionEntities))
                .build();
    }

    private List<Permission> permissionEntityListToPermissionList(List<PermissionEntity> permissionEntities) {
        if (permissionEntities == null) return new ArrayList<>();
        return permissionEntities.stream()
                .map(entity -> Permission.builder()
                        .permissionId(new PermissionId(entity.getId()))
                        .code(entity.getCode())
                        .description(entity.getDescription())
                        .domain(entity.getDomain())
                        .active(entity.isActive())
                        .createdAt(entity.getCreatedAt())
                        .updatedAt(entity.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}