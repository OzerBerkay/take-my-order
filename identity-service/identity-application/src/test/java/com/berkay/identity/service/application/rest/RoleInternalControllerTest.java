package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.domain.dto.role.SyncRolesQuery;
import com.berkay.identity.service.domain.dto.role.SyncRolesResponse;
import com.berkay.identity.service.ports.input.service.RoleApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RoleInternalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleApplicationService roleApplicationService;

    @InjectMocks
    private RoleInternalController roleInternalController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleInternalController).build();
    }

    @Test
    void shouldAllowAccess_WhenAuthorizedM2MTokenProvided() throws Exception {
        SyncRolesResponse response = SyncRolesResponse.builder().roles(List.of()).build();
        when(roleApplicationService.syncRoles(any(SyncRolesQuery.class))).thenReturn(response);

        mockMvc.perform(get("/internal/sync/roles"))
                .andExpect(status().isOk());
    }

}
