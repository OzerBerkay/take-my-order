package com.berkay.identity.service.infrastructure.keycloak.adapter;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.infrastructure.keycloak.config.KeycloakConfigData;
import com.berkay.identity.service.infrastructure.keycloak.exception.KeycloakIntegrationException;
import com.berkay.identity.service.infrastructure.keycloak.mapper.KeycloakMapper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakIdentityProviderAdapter implements IdentityProviderPort {

    private final Keycloak keycloak;
    private final KeycloakConfigData keycloakConfigData;
    private final KeycloakMapper keycloakMapper;

    @Override
    public String registerUser(User user, String password) {
        log.info("Initiating Keycloak user creation for email: {}", user.getEmail().getValue());

        UserRepresentation kcUser = keycloakMapper.getUserRepresentation(user, password);
        UsersResource usersResource = keycloak.realm(keycloakConfigData.getRealm()).users();

        // KRİTİK GÜVENLİK AĞI: JAX-RS Response objesi AutoCloseable'dır.
        // try-with-resources kullanmazsak arka planda TCP Connection Leak (Bağlantı
        // sızıntısı) oluşur!
        try (Response response = usersResource.create(kcUser)) {

            // 1. Durum: Başarılı Oluşturma (201)
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                String keycloakUserId = CreatedResponseUtil.getCreatedId(response);
                log.info("User successfully created in Keycloak! Keycloak ID: {}, Internal ID: {}",
                        keycloakUserId, user.getId().getValue());
                return keycloakUserId;

                // 2. Durum: Çakışma - Kullanıcı Zaten Var (409)
            } else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                log.warn("User already exists in Keycloak! Fetching existing ID for Email: {}", user.getEmail().getValue());
                java.util.List<UserRepresentation> existingUsers = usersResource.search(user.getEmail().getValue(), 0, 1);
                if (existingUsers != null && !existingUsers.isEmpty()) {
                    String existingId = existingUsers.get(0).getId();
                    log.info("Found existing Keycloak user ID: {} for Email: {}", existingId, user.getEmail().getValue());
                    return existingId;
                } else {
                    throw new KeycloakIntegrationException(
                            "User already exists in Keycloak but could not be fetched! Email: " + user.getEmail().getValue());
                }

                // 3. Durum: Beklenmeyen Hatalar (500, 403 vb.)
            } else {
                String errorMessage = response.readEntity(String.class); // Stream'i okuyup kapatır
                log.error("Failed to create user in Keycloak! Status: {}, Error: {}", response.getStatus(),
                        errorMessage);
                throw new KeycloakIntegrationException("Failed to create user in Keycloak! Error: " + errorMessage);
            }

        } catch (KeycloakIntegrationException e) {
            throw e; // Kendi ürettiğimiz kontrollü hata, doğrudan fırlat.
        } catch (Exception e) {
            // Keycloak sunucusuna ulaşılamaması (Network failure) durumunda
            log.error("Critical error while communicating with Keycloak Server! Error: {}", e.getMessage(), e);
            throw new KeycloakIntegrationException("Keycloak Server connection error! " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(String externalId) {
        log.info("Deleting user from Keycloak with ID: {}", externalId);
        try {
            UsersResource usersResource = keycloak.realm(keycloakConfigData.getRealm()).users();
            usersResource.get(externalId).remove();
            log.info("Successfully deleted user in Keycloak! External ID: {}", externalId);
        } catch (Exception e) {
            log.error("Error deleting user from Keycloak! External ID: {}", externalId, e);
            throw new KeycloakIntegrationException("Error deleting user from Keycloak! " + e.getMessage(), e);
        }
    }

    @Override
    public com.berkay.identity.service.dto.command.TokenResponse login(String username, String password) {
        log.info("Authenticating user: {}", username);
        try {
            String url = keycloakConfigData.getServerUrl() + "/realms/" + keycloakConfigData.getRealm()
                    + "/protocol/openid-connect/token";
            String body = "grant_type=password&client_id=" + keycloakConfigData.getClientId()
                    + "&client_secret=" + keycloakConfigData.getClientSecret()
                    + "&username=" + java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8)
                    + "&password=" + java.net.URLEncoder.encode(password, java.nio.charset.StandardCharsets.UTF_8);

            return executeTokenRequest(url, body);
        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            throw new KeycloakIntegrationException("Login failed! " + e.getMessage(), e);
        }
    }

    @Override
    public com.berkay.identity.service.dto.command.TokenResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        try {
            String url = keycloakConfigData.getServerUrl() + "/realms/" + keycloakConfigData.getRealm()
                    + "/protocol/openid-connect/token";
            String body = "grant_type=refresh_token&client_id=" + keycloakConfigData.getClientId()
                    + "&client_secret=" + keycloakConfigData.getClientSecret()
                    + "&refresh_token="
                    + java.net.URLEncoder.encode(refreshToken, java.nio.charset.StandardCharsets.UTF_8);

            return executeTokenRequest(url, body);
        } catch (com.berkay.identity.service.domain.exception.TokenExpiredDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Refresh token failed", e);
            throw new KeycloakIntegrationException("Refresh token failed! " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePassword(String externalId, String newPassword) {
        log.info("Updating password for user: {}", externalId);
        try {
            org.keycloak.representations.idm.CredentialRepresentation credential = new org.keycloak.representations.idm.CredentialRepresentation();
            credential.setTemporary(false);
            credential.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);

            UsersResource usersResource = keycloak.realm(keycloakConfigData.getRealm()).users();
            usersResource.get(externalId).resetPassword(credential);
        } catch (Exception e) {
            log.error("Error updating password in Keycloak! External ID: {}", externalId, e);
            throw new KeycloakIntegrationException("Error updating password in Keycloak! " + e.getMessage(), e);
        }
    }

    @Override
    public void resetPassword(String externalUserId, String newPassword) {
        log.info("Force resetting password for user: {}", externalUserId);
        try {
            org.keycloak.representations.idm.CredentialRepresentation credential = new org.keycloak.representations.idm.CredentialRepresentation();
            credential.setTemporary(false); // Changed from true so users can immediately log in
            credential.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);

            UsersResource usersResource = keycloak.realm(keycloakConfigData.getRealm()).users();
            usersResource.get(externalUserId).resetPassword(credential);
        } catch (Exception e) {
            log.error("Error force resetting password in Keycloak! External ID: {}", externalUserId, e);
            throw new KeycloakIntegrationException("Error force resetting password in Keycloak! " + e.getMessage(), e);
        }
    }

    @Override
    public void updateUserProfile(String externalUserId, String firstName, String lastName) {
        log.info("Updating user profile for user: {}", externalUserId);
        try {
            UserRepresentation user = new UserRepresentation();
            user.setFirstName(firstName);
            user.setLastName(lastName);

            UsersResource usersResource = keycloak.realm(keycloakConfigData.getRealm()).users();
            usersResource.get(externalUserId).update(user);
        } catch (Exception e) {
            log.error("Error updating user profile in Keycloak! External ID: {}", externalUserId, e);
            throw new KeycloakIntegrationException("Error updating user profile in Keycloak! " + e.getMessage(), e);
        }
    }

    private com.berkay.identity.service.dto.command.TokenResponse executeTokenRequest(String url, String body)
            throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(response.body());
            return com.berkay.identity.service.dto.command.TokenResponse.builder()
                    .accessToken(rootNode.path("access_token").asText())
                    .refreshToken(rootNode.path("refresh_token").asText())
                    .build();
        } else {
            log.error("Token request failed with status: {}, body: {}", response.statusCode(), response.body());
            if (response.statusCode() == 400 && response.body().contains("invalid_grant") && body.contains("grant_type=refresh_token")) {
                throw new com.berkay.identity.service.domain.exception.TokenExpiredDomainException("REFRESH_TOKEN_EXPIRED", "The provided refresh token is expired or inactive.");
            }
            throw new KeycloakIntegrationException("Token request failed with status: " + response.statusCode());
        }
    }

    @Override
    public void updateUserStatus(String externalUserId, boolean enabled) {
        log.info("Updating user status for user: {} to enabled: {}", externalUserId, enabled);
        try {
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(enabled);

            UsersResource usersResource = keycloak.realm(keycloakConfigData.getRealm()).users();
            usersResource.get(externalUserId).update(user);
        } catch (Exception e) {
            log.error("Error updating user status in Keycloak! External ID: {}", externalUserId, e);
            throw new KeycloakIntegrationException("Error updating user status in Keycloak! " + e.getMessage(), e);
        }
    }

    @Override
    public void updateUserRolesAndBranches(String externalUserId, java.util.List<String> roleIds, java.util.List<String> organizationalUnitIds) {
        log.info("Updating user roles and branches for user: {}", externalUserId);
        try {
            UsersResource usersResource = keycloak.realm(keycloakConfigData.getRealm()).users();
            UserRepresentation user = usersResource.get(externalUserId).toRepresentation();
            
            // Attributes map initializing
            java.util.Map<String, java.util.List<String>> attributes = user.getAttributes() != null 
                    ? new java.util.HashMap<>(user.getAttributes()) 
                    : new java.util.HashMap<>();
                    
            if (roleIds != null) {
                attributes.put("role_ids", roleIds);
            }
            if (organizationalUnitIds != null) {
                attributes.put("organizational_unit_ids", organizationalUnitIds);
            }
            
            user.setAttributes(attributes);

            usersResource.get(externalUserId).update(user);
        } catch (Exception e) {
            log.error("Error updating user roles and branches in Keycloak! External ID: {}", externalUserId, e);
            throw new KeycloakIntegrationException("Error updating user roles and branches in Keycloak! " + e.getMessage(), e);
        }
    }
}