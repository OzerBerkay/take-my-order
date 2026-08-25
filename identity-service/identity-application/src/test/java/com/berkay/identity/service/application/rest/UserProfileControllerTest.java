package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.command.UpdateUserProfileCommand;
import com.berkay.identity.service.dto.command.UpdateUserProfileResponse;
import com.berkay.identity.service.ports.input.service.UserApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserProfileController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.berkay.identity.service.application.security.config.SecurityConfig.class))
@ContextConfiguration(classes = {UserProfileController.class})
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserApplicationService userApplicationService;

    @Test
    @DisplayName("Başarılı Senaryo: Profil güncellenince HTTP 200 dönmelidir.")
    void shouldReturn200_WhenUpdateProfileIsSuccessful() throws Exception {
        UpdateUserProfileCommand command = UpdateUserProfileCommand.builder()
                .firstName("Updated First")
                .lastName("Updated Last")
                .imageUrl("https://example.com/updated.jpg")
                .build();

        UpdateUserProfileResponse response = UpdateUserProfileResponse.builder()
                .message("Profile successfully updated")
                .build();

        when(userApplicationService.updateUserProfile(any(UpdateUserProfileCommand.class))).thenReturn(response);

        mockMvc.perform(patch("/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile successfully updated"));
    }

    @Test
    @DisplayName("Alternatif Senaryo: Eksik bilgi ile profil güncellenmeye çalışıldığında HTTP 400 dönmelidir.")
    void shouldReturn400_WhenUpdateProfileCommandIsInvalid() throws Exception {
        UpdateUserProfileCommand command = UpdateUserProfileCommand.builder()
                // firstName and lastName must be present according to validation (assuming @NotBlank is on them)
                .build();

        mockMvc.perform(patch("/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Başarılı Senaryo: Kullanıcı kendi yetkilerini başarıyla çekebilmelidir.")
    void shouldReturn200_WhenGetMyPermissionsIsSuccessful() throws Exception {
        java.util.UUID userId = java.util.UUID.randomUUID();
        
        com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken jwtAuth = new com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken(
                java.util.UUID.randomUUID(), // externalId
                userId, // internalId
                com.berkay.identity.service.domain.valueobject.UserType.INTERNAL, // userType
                com.berkay.identity.service.domain.valueobject.AccountStatus.ACTIVE, // accountStatus
                "test@test.com", // email
                java.util.Collections.emptyList(), // roleIds
                java.util.Collections.emptyList(), // organizationalUnitIds
                "sid", // sid
                "token" // token
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(jwtAuth);

        java.util.UUID perm1Id = java.util.UUID.randomUUID();
        java.util.UUID perm2Id = java.util.UUID.randomUUID();
        
        com.berkay.identity.service.dto.query.PermissionResponse perm1 = com.berkay.identity.service.dto.query.PermissionResponse.builder()
                .id(perm1Id).name("can_do_x").description("desc").active(true).isRestricted(false).build();
        com.berkay.identity.service.dto.query.PermissionResponse perm2 = com.berkay.identity.service.dto.query.PermissionResponse.builder()
                .id(perm2Id).name("can_do_y").description("desc").active(true).isRestricted(false).build();
        
        com.berkay.identity.service.dto.query.RoleResponse role1 = com.berkay.identity.service.dto.query.RoleResponse.builder()
                .id(java.util.UUID.randomUUID()).name("Role 1").permissions(java.util.List.of(perm1)).build();
        com.berkay.identity.service.dto.query.RoleResponse role2 = com.berkay.identity.service.dto.query.RoleResponse.builder()
                .id(java.util.UUID.randomUUID()).name("Role 2").permissions(java.util.List.of(perm1, perm2)).build();

        com.berkay.identity.service.dto.query.UserResponse userResponse = com.berkay.identity.service.dto.query.UserResponse.builder()
                .id(userId)
                .roles(java.util.List.of(role1, role2))
                .build();

        when(userApplicationService.getUserProfile(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/users/me/permissions")
                        .principal(jwtAuth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.id == '" + perm1Id + "')]").exists())
                .andExpect(jsonPath("$[?(@.id == '" + perm2Id + "')]").exists());
                
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}
