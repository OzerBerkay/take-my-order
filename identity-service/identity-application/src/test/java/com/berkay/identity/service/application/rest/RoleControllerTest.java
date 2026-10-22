package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.application.security.auth.RoleAuthorizationService;
import com.berkay.identity.service.dto.command.role.CreateRoleCommand;
import com.berkay.identity.service.dto.command.role.CreateRoleResponse;
import com.berkay.identity.service.dto.command.role.DeleteRoleCommand;
import com.berkay.identity.service.dto.command.role.DeleteRoleResponse;
import com.berkay.identity.service.dto.command.role.UpdateRoleCommand;
import com.berkay.identity.service.dto.command.role.UpdateRoleResponse;
import com.berkay.identity.service.ports.input.service.RoleApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleApplicationService roleApplicationService;

    @Mock
    private RoleAuthorizationService roleAuthService;

    @InjectMocks
    private RoleController roleController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID roleId;
    private UUID restaurantId;
    private UUID permissionId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
        roleId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        permissionId = UUID.randomUUID();
    }

    @Test
    @DisplayName("POST /roles: Başarılı rol oluşturma")
    void shouldCreateRoleSuccessfully() throws Exception {
        CreateRoleCommand command = CreateRoleCommand.builder()
                .name("KITCHEN_STAFF")
                .organizationalUnitId(restaurantId)
                .permissionIds(Set.of(permissionId))
                .build();

        CreateRoleResponse response = CreateRoleResponse.builder()
                .roleId(roleId)
                .build();

        when(roleApplicationService.createRole(any(CreateRoleCommand.class))).thenReturn(response);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("application/vnd.api.v1+json")
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleId").value(roleId.toString()));

        verify(roleApplicationService).createRole(any(CreateRoleCommand.class));
    }

    @Test
    @DisplayName("PUT /roles/{roleId}: Başarılı rol güncelleme ve cache eviction")
    void shouldUpdateRoleSuccessfullyAndEvictCache() throws Exception {
        UpdateRoleCommand command = UpdateRoleCommand.builder()
                .name("SENIOR_KITCHEN_STAFF")
                .permissionIds(Set.of(permissionId))
                .build();

        UpdateRoleResponse response = UpdateRoleResponse.builder()
                .roleId(roleId)
                .build();

        when(roleApplicationService.updateRole(any(UpdateRoleCommand.class))).thenReturn(response);

        mockMvc.perform(put("/roles/{roleId}", roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("application/vnd.api.v1+json")
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId").value(roleId.toString()));

        verify(roleApplicationService).updateRole(any(UpdateRoleCommand.class));
        verify(roleAuthService).evictRoleCache(roleId);
    }

    @Test
    @DisplayName("DELETE /roles/{roleId}: Başarılı rol silme ve cache eviction")
    void shouldDeleteRoleSuccessfullyAndEvictCache() throws Exception {
        DeleteRoleResponse response = DeleteRoleResponse.builder()
                .roleId(roleId)
                .build();

        when(roleApplicationService.deleteRole(any(DeleteRoleCommand.class))).thenReturn(response);

        mockMvc.perform(delete("/roles/{roleId}", roleId)
                        .accept("application/vnd.api.v1+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId").value(roleId.toString()));

        verify(roleApplicationService).deleteRole(any(DeleteRoleCommand.class));
        verify(roleAuthService).evictRoleCache(roleId);
    }
}
