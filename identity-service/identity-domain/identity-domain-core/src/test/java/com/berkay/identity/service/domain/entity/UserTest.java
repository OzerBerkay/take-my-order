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

    @Test
    void shouldUpdateRolesForOrganizationalUnitAndKeepOtherOrgRolesAndBaseRoles() {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();

        Role merchantBase = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .isStatic(true)
                .build();

        Role oldRoleOrgA = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .organizationalUnitId(orgA)
                .isStatic(false)
                .build();

        Role roleOrgB = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .organizationalUnitId(orgB)
                .isStatic(false)
                .build();

        Role newRoleOrgA = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .organizationalUnitId(orgA)
                .isStatic(false)
                .build();

        User merchant = User.Builder.builder()
                .userId(new UserId(UUID.randomUUID()))
                .userType(UserType.MERCHANT)
                .roles(new java.util.ArrayList<>(java.util.List.of(merchantBase, oldRoleOrgA, roleOrgB)))
                .build();

        merchant.updateRolesForOrganizationalUnit(orgA, java.util.List.of(newRoleOrgA));

        org.junit.jupiter.api.Assertions.assertEquals(3, merchant.getRoles().size());
        org.junit.jupiter.api.Assertions.assertTrue(merchant.getRoles().stream().anyMatch(r -> r.getId().equals(merchantBase.getId())));
        org.junit.jupiter.api.Assertions.assertTrue(merchant.getRoles().stream().anyMatch(r -> r.getId().equals(roleOrgB.getId())));
        org.junit.jupiter.api.Assertions.assertTrue(merchant.getRoles().stream().anyMatch(r -> r.getId().equals(newRoleOrgA.getId())));
        org.junit.jupiter.api.Assertions.assertFalse(merchant.getRoles().stream().anyMatch(r -> r.getId().equals(oldRoleOrgA.getId())));
    }

    @Test
    void shouldClearRolesForOrganizationalUnitWhenEmptyListProvided() {
        UUID orgA = UUID.randomUUID();

        Role merchantBase = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .isStatic(true)
                .build();

        Role roleOrgA = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .organizationalUnitId(orgA)
                .isStatic(false)
                .build();

        User merchant = User.Builder.builder()
                .userId(new UserId(UUID.randomUUID()))
                .userType(UserType.MERCHANT)
                .roles(new java.util.ArrayList<>(java.util.List.of(merchantBase, roleOrgA)))
                .build();

        merchant.updateRolesForOrganizationalUnit(orgA, java.util.Collections.emptyList());

        org.junit.jupiter.api.Assertions.assertEquals(1, merchant.getRoles().size());
        org.junit.jupiter.api.Assertions.assertEquals(merchantBase.getId(), merchant.getRoles().get(0).getId());
    }

    @Test
    void shouldNotAllowUpdatingRolesForCustomer() {
        User customer = User.Builder.builder()
                .userId(new UserId(UUID.randomUUID()))
                .userType(UserType.CUSTOMER)
                .build();

        assertThrows(IdentityDomainException.class,
                () -> customer.updateRolesForOrganizationalUnit(UUID.randomUUID(), java.util.Collections.emptyList()));
    }

    @Test
    void shouldNotAllowAssigningRoleFromDifferentOrg() {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();

        Role roleOrgB = Role.builder()
                .roleId(new RoleId(UUID.randomUUID()))
                .organizationalUnitId(orgB)
                .isStatic(false)
                .build();

        User merchant = User.Builder.builder()
                .userId(new UserId(UUID.randomUUID()))
                .userType(UserType.MERCHANT)
                .build();

        assertThrows(IdentityDomainException.class,
                () -> merchant.updateRolesForOrganizationalUnit(orgA, java.util.List.of(roleOrgB)));
    }
}
