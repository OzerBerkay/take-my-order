package com.berkay.order.service.application.job;

import com.berkay.order.service.domain.ports.input.message.listener.role.RoleMessageListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleInitialSyncJobTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RoleMessageListener roleMessageListener;

    @InjectMocks
    private RoleInitialSyncJob roleInitialSyncJob;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(roleInitialSyncJob, "identityServiceUrl", "http://identity-service");
        ReflectionTestUtils.setField(roleInitialSyncJob, "keycloakUrl", "http://keycloak");
        ReflectionTestUtils.setField(roleInitialSyncJob, "keycloakRealm", "realm");
        ReflectionTestUtils.setField(roleInitialSyncJob, "clientId", "test-client");
        ReflectionTestUtils.setField(roleInitialSyncJob, "clientSecret", "secret");
        ReflectionTestUtils.setField(roleInitialSyncJob, "restTemplate", restTemplate);
    }

    @Test
    void shouldFetchM2mTokenAndPassToIdentityService() {
        // Mock Keycloak Token Response
        java.util.Map<String, Object> mockTokenResponse = new java.util.HashMap<>();
        mockTokenResponse.put("access_token", "mock-m2m-token");
        ResponseEntity<java.util.Map> tokenEntity = ResponseEntity.ok(mockTokenResponse);
        when(restTemplate.postForEntity(eq("http://keycloak/realms/realm/protocol/openid-connect/token"), any(HttpEntity.class), eq(java.util.Map.class)))
                .thenReturn(tokenEntity);

        // Mock Identity Service Response
        RoleInitialSyncJob.SyncRolesResponse responseBody = new RoleInitialSyncJob.SyncRolesResponse();
        responseBody.setRoles(List.of(new RoleInitialSyncJob.SyncRoleDto()));
        responseBody.setHasNextPage(false);
        ResponseEntity<RoleInitialSyncJob.SyncRolesResponse> responseEntity = ResponseEntity.ok(responseBody);
        
        when(restTemplate.exchange(
                eq("http://identity-service/internal/sync/roles?limit=100"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // Act
        roleInitialSyncJob.forceSyncRoles();

        // Assert
        verify(restTemplate).postForEntity(eq("http://keycloak/realms/realm/protocol/openid-connect/token"), any(HttpEntity.class), eq(java.util.Map.class));
        verify(restTemplate).exchange(
                eq("http://identity-service/internal/sync/roles?limit=100"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
        verify(roleMessageListener, times(1)).roleCreated(any());
    }
}
