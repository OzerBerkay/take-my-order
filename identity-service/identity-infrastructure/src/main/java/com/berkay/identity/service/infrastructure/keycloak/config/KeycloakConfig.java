package com.berkay.identity.service.infrastructure.keycloak.config;

import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final KeycloakConfigData keycloakConfigData;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakConfigData.getServerUrl())
                .realm(keycloakConfigData.getMasterRealm()) // Admin yetkisi olan realm (genelde master)
                .clientId(keycloakConfigData.getAdminClientId())
                .username(keycloakConfigData.getAdminUsername())
                .password(keycloakConfigData.getAdminPassword())
                .grantType(OAuth2Constants.PASSWORD) // Username/Password ile bağlanıyoruz
                .build();
    }
}