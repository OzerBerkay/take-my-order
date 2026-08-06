package com.berkay.identity.service.dataaccess.user.adapter;

import com.berkay.identity.service.dataaccess.user.entity.UserAddressEntity;
import com.berkay.identity.service.dataaccess.user.entity.UserEntity;
import com.berkay.identity.service.dataaccess.user.entity.UserRoleEntity;
import com.berkay.identity.service.dataaccess.role.entity.RoleEntity;
import com.berkay.identity.service.dataaccess.permission.entity.PermissionEntity;
import com.berkay.identity.service.dataaccess.role.entity.RolePermissionEntity;

import com.berkay.identity.service.dataaccess.user.exception.IdentityDataaccessException;
import com.berkay.identity.service.dataaccess.user.mapper.UserDataAccessMapper;

import com.berkay.identity.service.dataaccess.user.repository.UserAddressJpaRepository;
import com.berkay.identity.service.dataaccess.user.repository.UserJpaRepository;
import com.berkay.identity.service.dataaccess.user.repository.UserRoleJpaRepository;
import com.berkay.identity.service.dataaccess.role.repository.RoleJpaRepository;
import com.berkay.identity.service.dataaccess.permission.repository.PermissionJpaRepository;
import com.berkay.identity.service.dataaccess.role.repository.RolePermissionJpaRepository;

import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final UserAddressJpaRepository addressJpaRepository;
    private final UserRoleJpaRepository userRoleJpaRepository;
    private final RolePermissionJpaRepository rolePermissionJpaRepository;
    private final PermissionJpaRepository permissionJpaRepository;
    private final UserDataAccessMapper userDataAccessMapper;

    @Override
    public User save(User user) {
        Optional<UserEntity> existingOpt = userJpaRepository.findById(user.getId().getValue());
        UserEntity entityToSave;

        if (existingOpt.isPresent()) {
            entityToSave = existingOpt.get();
            // Update fields manually
            entityToSave.setFirstName(user.getFirstName().getValue());
            entityToSave.setLastName(user.getLastName().getValue());
            entityToSave.setPhoneNumber(user.getPhoneNumber().getValue());
            entityToSave.setImageUrl(user.getImageUrl());
            entityToSave.setStatus(user.getStatus());
            entityToSave.setUpdatedAt(user.getUpdatedAt());
            
            // Clear and add all for ElementCollection
            List<UUID> newOrgUnits = user.getOrganizationalUnitIds() != null ? new ArrayList<>(user.getOrganizationalUnitIds()) : new ArrayList<>();
            if (entityToSave.getOrganizationalUnitIds() != null) {
                entityToSave.getOrganizationalUnitIds().clear();
                entityToSave.getOrganizationalUnitIds().addAll(newOrgUnits);
            } else {
                entityToSave.setOrganizationalUnitIds(newOrgUnits);
            }
        } else {
            entityToSave = userDataAccessMapper.userToUserEntity(user);
        }

        UserEntity savedUserEntity = userJpaRepository.save(entityToSave);

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            userRoleJpaRepository.deleteByUserId(savedUserEntity.getId());
            List<UserRoleEntity> userRoleEntities = user.getRoles().stream().map(r ->
                    UserRoleEntity.builder()
                            .userId(savedUserEntity.getId())
                            .roleId(r.getId().getValue())
                            .build()
            ).collect(Collectors.toList());
            userRoleJpaRepository.saveAll(userRoleEntities);
        }

        return findById(new com.berkay.identity.service.domain.valueobject.UserId(savedUserEntity.getId())).orElseThrow();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(this::reconstructUser);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userJpaRepository.findByPhoneNumber(phoneNumber).map(this::reconstructUser);
    }

    @Override
    public Optional<User> findByExternalId(String externalId) {
        return userJpaRepository.findByExternalId(externalId).map(this::reconstructUser);
    }

    @Override
    public Optional<User> findById(com.berkay.identity.service.domain.valueobject.UserId id) {
        return userJpaRepository.findById(id.getValue()).map(this::reconstructUser);
    }

    @Override
    public List<Role> findRolesByIds(List<RoleId> roleIds) {
        List<UUID> roleUuids = roleIds.stream()
                .map(RoleId::getValue)
                .collect(Collectors.toList());
        return roleJpaRepository.findAllById(roleUuids).stream()
                .map(this::reconstructRole)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Role> findRoleByName(String roleName) {
        return roleJpaRepository.findByName(roleName).map(this::reconstructRole);
    }

    private User reconstructUser(UserEntity userEntity) {
        List<UUID> roleIds = userRoleJpaRepository.findByUserId(userEntity.getId()).stream()
                .map(UserRoleEntity::getRoleId)
                .collect(Collectors.toList());
        
        List<Role> roles = roleJpaRepository.findAllById(roleIds).stream()
                .map(this::reconstructRole)
                .collect(Collectors.toList());

        return userDataAccessMapper.userEntityToUserWithCollections(userEntity, new ArrayList<>(), roles);
    }

    private Role reconstructRole(RoleEntity roleEntity) {
        List<UUID> permissionIds = rolePermissionJpaRepository.findByRoleId(roleEntity.getId()).stream()
                .map(RolePermissionEntity::getPermissionId)
                .collect(Collectors.toList());
        
        List<PermissionEntity> permissions = permissionJpaRepository.findAllById(permissionIds);
        
        return userDataAccessMapper.roleEntityToRoleWithPermissions(roleEntity, permissions);
    }
}