package com.berkay.identity.service.infrastructure.keycloak.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "identity-service.keycloak")
public class KeycloakConfigData {
    private String serverUrl;
    private String realm;
    private String masterRealm;
    private String adminClientId;
    private String adminUsername;
    private String adminPassword;
    private String clientId;
    private String clientSecret;
}