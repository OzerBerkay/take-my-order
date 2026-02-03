package com.berkay.identity.service.dataaccess.user.mapper;

import com.berkay.identity.service.dataaccess.user.entity.AddressEntity;
import com.berkay.identity.service.dataaccess.user.entity.PermissionEntity;
import com.berkay.identity.service.dataaccess.user.entity.RoleEntity;
import com.berkay.identity.service.dataaccess.user.entity.UserEntity;
import com.berkay.identity.service.domain.entity.Address;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserDataAccessMapper {

    public UserEntity userToUserEntity(User user) {
        UserEntity userEntity = UserEntity.builder()
                .id(user.getId().getValue())
                .email(user.getEmail().getValue())
                .phoneNumber(user.getPhoneNumber().getValue())
                .firstName(user.getFirstName().getValue())
                .lastName(user.getLastName().getValue())
                .imageUrl(user.getImageUrl())
                .isEmailVerified(user.isEmailVerified())
                .isPhoneVerified(user.isPhoneVerified())
                .userType(user.getUserType())
                .status(user.getStatus())
                .addresses(addressListToAddressEntityList(user.getAddresses()))
                .roles(roleListToRoleEntitySet(user.getRoles())) // List -> Set Dönüşümü
                .build();

        // Audit alanlarını Domain'den alıp Entity'ye set ediyoruz
        userEntity.setCreatedAt(user.getCreatedAt());
        userEntity.setUpdatedAt(user.getUpdatedAt());

        return userEntity;
    }

    public User userEntityToUser(UserEntity userEntity) {
        return User.builder()
                .userId(new UserId(userEntity.getId()))
                .email(new UserEmail(userEntity.getEmail()))
                .phoneNumber(new PhoneNumber(userEntity.getPhoneNumber()))
                .firstName(new FirstName(userEntity.getFirstName()))
                .lastName(new LastName(userEntity.getLastName()))
                .imageUrl(userEntity.getImageUrl())
                .isEmailVerified(userEntity.getIsEmailVerified())
                .isPhoneVerified(userEntity.getIsPhoneVerified())
                .userType(userEntity.getUserType())
                .status(userEntity.getStatus())
                .addresses(addressEntityListToAddressList(userEntity.getAddresses()))
                .roles(roleEntitySetToRoleList(userEntity.getRoles())) // Set -> List Dönüşümü
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .build();
    }

    // HELPER METHODS (Private)

    private List<AddressEntity> addressListToAddressEntityList(List<Address> addresses) {
        if (addresses == null) return new ArrayList<>();
        return addresses.stream()
                .map(address -> AddressEntity.builder()
                        .id(address.getId().getValue())
                        .name(address.getName())
                        .street(address.getStreet())
                        .city(address.getCity())
                        .postalCode(address.getPostalCode())
                        .country(address.getCountry())
                        .build()) // User atamasını Adapter içinde veya JPA ilişki yönetiminde halledeceğiz
                .collect(Collectors.toList());
    }

    private List<Address> addressEntityListToAddressList(List<AddressEntity> addressEntities) {
        if (addressEntities == null) return new ArrayList<>();
        return addressEntities.stream()
                .map(entity -> Address.builder()
                        .addressId(new AddressId(entity.getId()))
                        .name(entity.getName())
                        .street(entity.getStreet())
                        .city(entity.getCity())
                        .postalCode(entity.getPostalCode())
                        .country(entity.getCountry())
                        .build())
                .collect(Collectors.toList());
    }

    // List<Role> -> Set<RoleEntity>
    private Set<RoleEntity> roleListToRoleEntitySet(List<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(role -> RoleEntity.builder()
                        .id(role.getId().getValue())
                        .name(role.getName())
                        // Permission'ları RoleEntity'ye set etmiyoruz, çünkü Role veritabanında zaten var.
                        // Sadece User-Role ilişkisi kuruluyor.
                        .build())
                .collect(Collectors.toSet());
    }

    // Set<RoleEntity> -> List<Role>
    private List<Role> roleEntitySetToRoleList(Set<RoleEntity> roleEntities) {
        if (roleEntities == null) return new ArrayList<>();
        return new ArrayList<>(roleEntities.stream()
                .map(this::roleEntityToRole)
                .collect(Collectors.toList()));
    }

    public Role roleEntityToRole(RoleEntity entity) {
        if (entity == null) return null;
        return new Role(
                new RoleId(entity.getId()),
                entity.getName(),
                permissionEntitySetToPermissionList(entity.getPermissions())
        );
    }

    private List<Permission> permissionEntitySetToPermissionList(Set<PermissionEntity> permissionEntities) {
        if (permissionEntities == null) return new ArrayList<>();
        return permissionEntities.stream()
                .map(entity -> new Permission(
                        new PermissionId(entity.getId()),
                        entity.getName()
                ))
                .collect(Collectors.toList());
    }
}