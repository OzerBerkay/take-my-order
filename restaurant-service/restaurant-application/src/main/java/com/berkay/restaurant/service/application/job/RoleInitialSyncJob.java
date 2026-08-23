package com.berkay.restaurant.service.application.job;

import com.berkay.restaurant.service.domain.ports.input.message.listener.role.RoleMessageListener;
import com.berkay.restaurant.service.domain.dto.message.PermissionPayload;
import com.berkay.restaurant.service.domain.dto.message.RoleEventPayload;
import com.berkay.restaurant.service.domain.ports.output.repository.RoleRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RoleInitialSyncJob {

    private final RoleMessageListener roleMessageListener;
    private final RoleRepository roleRepository;
    private final RestTemplate restTemplate;


    @Value("${identity-service.url:http://localhost:8184}")
    private String identityServiceUrl;

    @Value("${KEYCLOAK_SERVER_URL:http://localhost:30080}")
    private String keycloakUrl;

    @Value("${KEYCLOAK_REALM:take-my-order-realm}")
    private String keycloakRealm;

    @Value("${KEYCLOAK_CLIENT_ID:take-my-order-client}")
    private String clientId;

    @Value("${KEYCLOAK_CLIENT_SECRET:local-dummy-secret}")
    private String clientSecret;


    public RoleInitialSyncJob(RoleMessageListener roleMessageListener,
                              RoleRepository roleRepository) {
        this.roleMessageListener = roleMessageListener;
        this.roleRepository = roleRepository;
        this.restTemplate = new RestTemplate();
    }
    @EventListener(ApplicationReadyEvent.class)
    public void syncRolesOnStartup() {
        long count = roleRepository.count();
        if (count > 0) {
            log.info("Role replica table is not empty (count: {}). Skipping initial sync.", count);
            return;
        }
        executeSync();
    }

    public void forceSyncRoles() {
        log.info("Manual force sync requested. Executing sync ignoring current count...");
        executeSync();
    }

    private void executeSync() {

        log.info("Starting role sync from identity-service...");
        String cursor = null;
        boolean hasNextPage = true;

        while (hasNextPage) {
            try {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(identityServiceUrl + "/internal/sync/roles")
                        .queryParam("limit", 100);

                if (cursor != null) {
                    builder.queryParam("cursor", cursor);
                }


                String token = getM2mToken();
                HttpHeaders headers = new HttpHeaders();
                if (token != null) {
                    headers.setBearerAuth(token);
                }
                HttpEntity<?> entity = new HttpEntity<>(headers);

                ResponseEntity<SyncRolesResponse> response = restTemplate.exchange(
                        builder.toUriString(),
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<SyncRolesResponse>() {}
                );


                SyncRolesResponse body = response.getBody();
                if (body != null && body.getRoles() != null) {
                    log.info("Fetched {} roles from identity-service.", body.getRoles().size());
                    for (SyncRoleDto dto : body.getRoles()) {
                        RoleEventPayload payload = mapToPayload(dto);
                        roleMessageListener.roleCreated(payload);
                    }

                    hasNextPage = body.isHasNextPage();
                    if (hasNextPage && body.getNextCursor() != null) {
                        cursor = body.getNextCursor();
                    } else {
                        hasNextPage = false;
                    }
                } else {
                    hasNextPage = false;
                }
            } catch (Exception e) {
                log.error("Failed to sync roles from identity-service. Will retry or rely on Kafka. Error: {}", e.getMessage());
                hasNextPage = false; // Stop on error
            }
        }

        log.info("Initial role sync completed successfully.");
    }


    private String getM2mToken() {
        try {
            String tokenUrl = keycloakUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<java.util.Map> response = restTemplate.postForEntity(tokenUrl, request, java.util.Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
        } catch (Exception e) {
            log.error("Failed to retrieve M2M token from Keycloak: {}", e.getMessage());
        }
        return null;
    }

    private RoleEventPayload mapToPayload(SyncRoleDto dto) {
        List<PermissionPayload> permissions = null;
        if (dto.getPermissions() != null) {
            permissions = dto.getPermissions().stream().map(p -> PermissionPayload.builder()
                    .id(p.getId())
                    .code(p.getCode())
                    .domain(p.getDomain())
                    .isActive(p.getIsActive())
                    .isRestricted(p.getIsRestricted())
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build()).collect(Collectors.toList());
        }

        return RoleEventPayload.builder()
                .roleId(dto.getId())
                .name(dto.getName())
                .userType(dto.getUserType())
                .organizationalUnitId(dto.getOrganizationalUnitId())
                .eventType("ROLE_CREATED")
                .version(dto.getUpdatedAt() != null ? java.time.ZonedDateTime.parse(dto.getUpdatedAt()).toInstant().toEpochMilli() : 0L)
                .permissions(permissions)
                .build();
    }

    @Data
    public static class SyncRolesResponse {
        private List<SyncRoleDto> roles;
        private String nextCursor;
        private boolean hasNextPage;
    }

    @Data
    public static class SyncRoleDto {
        private UUID id;
        private String name;
        private String userType;
        private UUID organizationalUnitId;
        private List<SyncPermissionDto> permissions;
        private String updatedAt;
    }

    @Data
    public static class SyncPermissionDto {
        private UUID id;
        private String code;
        private String domain;
        private Boolean isActive;
        private Boolean isRestricted;
        private String createdAt;
        private String updatedAt;
    }
}
