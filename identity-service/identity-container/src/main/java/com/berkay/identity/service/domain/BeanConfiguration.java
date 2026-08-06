package com.berkay.identity.service.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;

@Configuration
public class BeanConfiguration {

    @Bean
    public IdentityDomainService identityDomainService() {
        return new IdentityDomainServiceImpl();
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "identity-service.init", name = "reset-db", havingValue = "true")
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "identity-service.init", name = "reset-keycloak", havingValue = "true")
    @org.springframework.core.annotation.Order(1)
    public org.springframework.boot.CommandLineRunner keycloakCleaner(
            org.keycloak.admin.client.Keycloak keycloak,
            com.berkay.identity.service.infrastructure.keycloak.config.KeycloakConfigData keycloakConfigData) {
        return args -> {
            System.out.println("Cleaning up all users from Keycloak realm: " + keycloakConfigData.getRealm());
            try {
                java.util.List<org.keycloak.representations.idm.UserRepresentation> users =
                        keycloak.realm(keycloakConfigData.getRealm()).users().list();
                for (org.keycloak.representations.idm.UserRepresentation user : users) {
                    keycloak.realm(keycloakConfigData.getRealm()).users().delete(user.getId());
                }
                System.out.println("Keycloak users cleaned up successfully.");
            } catch (Exception e) {
                System.err.println("Failed to clean up Keycloak users: " + e.getMessage());
            }
        };
    }
}