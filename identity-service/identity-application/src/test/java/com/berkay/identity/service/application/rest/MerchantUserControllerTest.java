package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.command.RegisterMerchantCommand;
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

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantUserController.class)
@org.springframework.context.annotation.Import(com.berkay.identity.service.application.security.config.SecurityConfig.class)
@org.springframework.test.context.ContextConfiguration(classes = {MerchantUserController.class, com.berkay.identity.service.application.security.config.SecurityConfig.class})
public class MerchantUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserApplicationService userApplicationService;

    @org.springframework.boot.test.mock.mockito.SpyBean
    private com.berkay.identity.service.application.security.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean(name = "roleAuthService")
    private com.berkay.identity.service.application.security.auth.RoleAuthorizationService roleAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getMerchantUsers_WithCorrectAuthority_ShouldReturnUsers() throws Exception {
        Mockito.when(roleAuthService.hasPermissionForOrg(Mockito.any(), Mockito.any(), Mockito.eq("can_view_merchant_users"))).thenReturn(true);
        Mockito.when(userApplicationService.getMerchantUsers(Mockito.any())).thenReturn(com.berkay.identity.service.dto.query.PageResult.<com.berkay.identity.service.dto.query.MerchantUserResponse>builder().build());

        // Test passing query parameter orgUnitId to ensure isolation test structure exists
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/merchant/users")
                .param("orgUnitId", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMerchantUsers_WithWrongAuthority_ShouldReturnForbidden() throws Exception {
        Mockito.when(roleAuthService.hasPermissionForOrg(Mockito.any(), Mockito.any(), Mockito.eq("can_view_merchant_users"))).thenReturn(false);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/merchant/users")
                .param("orgUnitId", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updateUserRoles_WithCanAssignRolePermission_ShouldReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orgUnitId = UUID.randomUUID();
        com.berkay.identity.service.application.rest.dto.UpdateMerchantUserRolesRequest request =
                new com.berkay.identity.service.application.rest.dto.UpdateMerchantUserRolesRequest(java.util.List.of(UUID.randomUUID()));

        Mockito.when(roleAuthService.hasPermissionForOrg(Mockito.any(), Mockito.eq(orgUnitId), Mockito.eq("can_assign_role"))).thenReturn(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/merchant/users/{userId}/roles", userId)
                .with(csrf())
                .param("orgUnitId", orgUnitId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(userApplicationService).updateMerchantUserRoles(Mockito.any());
    }

    @Test
    @WithMockUser
    void updateUserRoles_WithoutCanAssignRolePermission_ShouldReturnForbidden() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orgUnitId = UUID.randomUUID();
        com.berkay.identity.service.application.rest.dto.UpdateMerchantUserRolesRequest request =
                new com.berkay.identity.service.application.rest.dto.UpdateMerchantUserRolesRequest(java.util.List.of(UUID.randomUUID()));

        Mockito.when(roleAuthService.hasPermissionForOrg(Mockito.any(), Mockito.eq(orgUnitId), Mockito.eq("can_assign_role"))).thenReturn(false);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/merchant/users/{userId}/roles", userId)
                .with(csrf())
                .param("orgUnitId", orgUnitId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        Mockito.verify(userApplicationService, Mockito.never()).updateMerchantUserRoles(Mockito.any());
    }

    @Test
    @WithMockUser
    void updateUserRoles_WithoutOrgUnitId_ShouldReturnBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        com.berkay.identity.service.application.rest.dto.UpdateMerchantUserRolesRequest request =
                new com.berkay.identity.service.application.rest.dto.UpdateMerchantUserRolesRequest(java.util.List.of(UUID.randomUUID()));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/merchant/users/{userId}/roles", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
