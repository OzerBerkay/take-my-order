package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.application.rest.dto.ForceResetPasswordRequest;
import com.berkay.identity.service.dto.command.ForceResetPasswordCommand;
import com.berkay.identity.service.ports.input.service.UserApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.berkay.identity.service.handler.AdminUserCommandHandler;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@org.springframework.context.annotation.Import(com.berkay.identity.service.application.security.config.SecurityConfig.class)
@org.springframework.test.context.ContextConfiguration(classes = {AdminUserController.class, com.berkay.identity.service.application.security.config.SecurityConfig.class})
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserApplicationService userApplicationService;

    @org.springframework.boot.test.mock.mockito.SpyBean
    private com.berkay.identity.service.application.security.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private AdminUserCommandHandler adminUserCommandHandler;

    @MockBean(name = "roleAuthService")
    private com.berkay.identity.service.application.security.auth.RoleAuthorizationService roleAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void forceResetPassword_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        UUID userId = UUID.randomUUID();
        ForceResetPasswordRequest request = new ForceResetPasswordRequest("newPassword123!");

        mockMvc.perform(put("/admin/users/{userId}/password", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    private com.berkay.application.security.JwtAuthenticationToken getAdminAuth() {
        return new com.berkay.application.security.JwtAuthenticationToken(
                UUID.randomUUID(),
                "externalId",
                "INTERNAL",
                java.util.List.of(),
                java.util.List.of(),
                "sid",
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void forceResetPassword_WithWrongAuthority_ShouldReturnForbidden() throws Exception {
        UUID userId = UUID.randomUUID();
        ForceResetPasswordRequest request = new ForceResetPasswordRequest("newPassword123!");

        Mockito.when(roleAuthService.hasPermission(Mockito.any(), Mockito.eq("can_reset_password"))).thenReturn(false);

        mockMvc.perform(put("/admin/users/{userId}/password", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(getAdminAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void forceResetPassword_WithCorrectAuthority_ShouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        ForceResetPasswordRequest request = new ForceResetPasswordRequest("newPassword123!");

        Mockito.when(roleAuthService.hasPermission(Mockito.any(), Mockito.eq("can_reset_password"))).thenReturn(true);
        Mockito.doNothing().when(userApplicationService).forceResetPassword(Mockito.any(ForceResetPasswordCommand.class));

        mockMvc.perform(put("/admin/users/{userId}/password", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(getAdminAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAdminUsers_WithCorrectAuthority_ShouldReturnUsers() throws Exception {
        Mockito.when(roleAuthService.hasPermission(Mockito.any(), Mockito.eq("can_view_users"))).thenReturn(true);
        Mockito.when(userApplicationService.getAdminUsers(Mockito.any())).thenReturn(com.berkay.identity.service.dto.query.PageResult.<com.berkay.identity.service.dto.query.UserResponse>builder().build());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/users")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(getAdminAuth()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAdminUsers_WithWrongAuthority_ShouldReturnForbidden() throws Exception {
        Mockito.when(roleAuthService.hasPermission(Mockito.any(), Mockito.eq("can_view_users"))).thenReturn(false);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/users")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(getAdminAuth()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserStatus_WithCorrectAuthority_ShouldReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        com.berkay.identity.service.application.rest.dto.UpdateUserStatusRequest requestCommand = 
                new com.berkay.identity.service.application.rest.dto.UpdateUserStatusRequest(com.berkay.identity.service.domain.valueobject.AccountStatus.BANNED);

        Mockito.when(roleAuthService.hasPermission(Mockito.any(), Mockito.eq("can_update_user_status"))).thenReturn(true);
        com.berkay.identity.service.dto.command.UpdateUserStatusResponse mockResponse = com.berkay.identity.service.dto.command.UpdateUserStatusResponse.builder()
                .userId(userId)
                .message("Status successfully updated")
                .build();
                
        Mockito.when(userApplicationService.updateUserStatus(Mockito.any())).thenReturn(mockResponse);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/admin/users/{userId}/status", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCommand))
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void updateUserStatus_WithWrongAuthority_ShouldReturnForbidden() throws Exception {
        UUID userId = UUID.randomUUID();
        com.berkay.identity.service.application.rest.dto.UpdateUserStatusRequest requestCommand = 
                new com.berkay.identity.service.application.rest.dto.UpdateUserStatusRequest(com.berkay.identity.service.domain.valueobject.AccountStatus.BANNED);
        
        Mockito.when(roleAuthService.hasPermission(Mockito.any(), Mockito.eq("can_update_user_status"))).thenReturn(false);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/admin/users/{userId}/status", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCommand))
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(getAdminAuth())))
                .andExpect(status().isForbidden());
    }
}
