package com.berkay.identity.service.mapper;

import com.berkay.identity.service.domain.entity.Address;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.*;
import com.berkay.identity.service.dto.command.CreateAddressCommand;
import com.berkay.identity.service.dto.command.RegisterInternalUserCommand;
import com.berkay.identity.service.dto.command.RegisterCustomerCommand;
import com.berkay.identity.service.dto.command.RegisterMerchantCommand;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDataMapper {

    // Customer Mapping
    public User registerCustomerCommandToUser(RegisterCustomerCommand command, Role role) {
        return User.builder()
                .email(new UserEmail(command.getEmail()))
                .firstName(new FirstName(command.getFirstName()))
                .lastName(new LastName(command.getLastName()))
                .phoneNumber(new PhoneNumber(command.getPhoneNumber()))
                .authProvider(AuthProvider.LOCAL) // VARSAYILAN
                .roles(new ArrayList<>(Collections.singletonList(role)))
                // Initialize metodu Type, Status ayarlayacak, o yüzden burada boş bırakıyoruz
                .build();
    }

    // Merchant Mapping
    public User registerMerchantCommandToUser(RegisterMerchantCommand command, Role role) {
        return User.builder()
                .email(new UserEmail(command.getEmail()))
                .firstName(new FirstName(command.getFirstName()))
                .lastName(new LastName(command.getLastName()))
                .phoneNumber(new PhoneNumber(command.getPhoneNumber()))
                .authProvider(AuthProvider.LOCAL)// VARSAYILAN
                // new ArrayList<>(...) mutable olması ve gelecekte rol eklenmesi gerekirse patlamaması için gerekli
                // Collections.singletonList(role) immutable çalışmakta
                .roles(new ArrayList<>(Collections.singletonList(role)))
                // Status ve UserType -> initiateMerchant() içinde atanacak.
                .build();
    }

    // Internal Mapping (Rolleri dışarıdan alır)
    public User registerInternalUserCommandToUser(RegisterInternalUserCommand command, List<Role> roles) {
        return User.builder()
                .email(new UserEmail(command.getEmail()))
                .firstName(new FirstName(command.getFirstName()))
                .lastName(new LastName(command.getLastName()))
                .phoneNumber(new PhoneNumber(command.getPhoneNumber()))
                .authProvider(AuthProvider.LOCAL) // VARSAYILAN
                .roles(new ArrayList<>(roles)) // Internal user rolleri buradan alır
                .build();
    }

    public CreateUserResponse userToCreateUserResponse(User user, String message) {
        return CreateUserResponse.builder()
                .userId(user.getId().getValue())
                .message(message)
                .build();
    }

    public com.berkay.identity.service.dto.query.UserResponse userToUserResponse(User user, List<java.util.UUID> allowedOrgUnitIds) {
        return com.berkay.identity.service.dto.query.UserResponse.builder()
                .id(user.getId().getValue())
                .externalId(user.getExternalId())
                .email(user.getEmail().getValue())
                .firstName(user.getFirstName().getValue())
                .lastName(user.getLastName().getValue())
                .phoneNumber(user.getPhoneNumber().getValue())
                .userType(user.getUserType())
                .status(user.getStatus())
                .authProvider(user.getAuthProvider())
                .isEmailVerified(user.isEmailVerified())
                .isPhoneVerified(user.isPhoneVerified())
                .imageUrl(user.getImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .organizationalUnitIds(user.getOrganizationalUnitIds())
                .roles(user.getRoles().stream()
                        .filter(role -> {
                            if (allowedOrgUnitIds == null) return true; // Admin gets everything
                            if (role.getOrganizationalUnitId() == null) return false; // Exclude global roles for merchant
                            return allowedOrgUnitIds.contains(role.getOrganizationalUnitId());
                        })
                        .map(this::roleToRoleResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private com.berkay.identity.service.dto.query.RoleResponse roleToRoleResponse(Role role) {
        return com.berkay.identity.service.dto.query.RoleResponse.builder()
                .id(role.getId().getValue())
                .name(role.getName())
                .isStatic(role.isStatic())
                .organizationalUnitId(role.getOrganizationalUnitId())
                .userType(role.getUserType().name())
                .createdAt(role.getCreatedAt())
                .permissions(role.getPermissions().stream()
                        .map(p -> com.berkay.identity.service.dto.query.PermissionResponse.builder()
                                .id(p.getId().getValue())
                                .name(p.getCode())
                                .description(p.getDescription())
                                .active(p.isActive())
                                .isRestricted(p.isRestricted())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}