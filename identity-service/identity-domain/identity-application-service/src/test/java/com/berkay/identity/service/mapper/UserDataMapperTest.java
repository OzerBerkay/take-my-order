package com.berkay.identity.service.mapper;

import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.*;
import com.berkay.identity.service.dto.query.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDataMapperTest {

    private UserDataMapper userDataMapper;

    @BeforeEach
    void setUp() {
        userDataMapper = new UserDataMapper();
    }

    @Test
    void userToUserResponse_ShouldMapAllFieldsCorrectly() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID orgUnitId = UUID.randomUUID();
        
        Role role = Role.builder()
                .roleId(new RoleId(roleId))
                .name("ADMIN")
                .isStatic(false)
                .organizationalUnitId(orgUnitId)
                .userType(UserType.INTERNAL)
                .permissions(Collections.emptyList())
                .build();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        User user = User.builder()
                .userId(new UserId(userId))
                .externalId("ext-123")
                .email(new UserEmail("test@test.com"))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .phoneNumber(new PhoneNumber("+905555555555"))
                .userType(UserType.INTERNAL)
                .status(AccountStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .isEmailVerified(true)
                .isPhoneVerified(false)
                .imageUrl("http://image.url")
                .createdAt(now)
                .updatedAt(now)
                .roles(Collections.singletonList(role))
                .organizationalUnitIds(Collections.singletonList(orgUnitId))
                .build();

        // Act
        UserResponse response = userDataMapper.userToUserResponse(user, null);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("ext-123", response.getExternalId());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("+905555555555", response.getPhoneNumber());
        assertEquals(UserType.INTERNAL, response.getUserType());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(AuthProvider.LOCAL, response.getAuthProvider());
        assertTrue(response.isEmailVerified());
        assertFalse(response.isPhoneVerified());
        assertEquals("http://image.url", response.getImageUrl());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
        
        assertTrue(response.getOrganizationalUnitIds().contains(orgUnitId));
        assertEquals(1, response.getRoles().size());
        assertEquals(roleId, response.getRoles().get(0).getId());
    }
}
