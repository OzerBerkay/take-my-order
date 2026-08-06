package com.berkay.identity.service.domain.entity;

import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {

    @Test
    void shouldNotAllowAddingRoleToCustomer() {
        User customer = User.Builder.builder()
                .userId(new UserId(UUID.randomUUID()))
                .userType(UserType.CUSTOMER)
                .build();

        Role role = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .isStatic(false)
                .build();

        assertThrows(IdentityDomainException.class, () -> customer.addRole(role));
    }

    @Test
    void shouldNotAllowRemovingRoleFromCustomer() {
        User customer = User.Builder.builder()
                .userId(new UserId(UUID.randomUUID()))
                .userType(UserType.CUSTOMER)
                .build();

        Role role = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .isStatic(false)
                .build();

        assertThrows(IdentityDomainException.class, () -> customer.removeRole(role));
    }

    @Test
    void shouldNotAllowAddingStaticRole() {
        User merchant = User.Builder.builder()
                .userId(new UserId(UUID.randomUUID()))
                .userType(UserType.MERCHANT)
                .build();

        Role staticRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .isStatic(true)
                .build();

        assertThrows(IdentityDomainException.class, () -> merchant.addRole(staticRole));
    }

    @Test
    void shouldNotAllowRemovingStaticRole() {
        User merchant = User.Builder.builder()
                .userId(new UserId(UUID.randomUUID()))
                .userType(UserType.MERCHANT)
                .build();

        Role staticRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .isStatic(true)
                .build();

        assertThrows(IdentityDomainException.class, () -> merchant.removeRole(staticRole));
    }

    @Test
    void shouldAllowAddingAndRemovingCustomRoleForMerchant() {
        User merchant = User.Builder.builder()
                .userId(new UserId(UUID.randomUUID()))
                .userType(UserType.MERCHANT)
                .build();

        Role customRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .isStatic(false)
                .build();

        assertDoesNotThrow(() -> merchant.addRole(customRole));
        assertDoesNotThrow(() -> merchant.removeRole(customRole));
    }

    @Test
    void shouldAllowAddingAndRemovingCustomRoleForInternalUser() {
        User internalUser = User.Builder.builder()
                .userId(new UserId(UUID.randomUUID()))
                .userType(UserType.INTERNAL)
                .build();

        Role customRole = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .isStatic(false)
                .build();

        assertDoesNotThrow(() -> internalUser.addRole(customRole));
        assertDoesNotThrow(() -> internalUser.removeRole(customRole));
    }
}
