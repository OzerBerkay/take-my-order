package com.berkay.identity.service.domain;

import com.berkay.identity.service.infrastructure.keycloak.config.KeycloakConfigData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.CommandLineRunner;

import static org.assertj.core.api.Assertions.assertThat;

class BeanConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BeanConfiguration.class))
            .withBean(Keycloak.class, () -> org.mockito.Mockito.mock(Keycloak.class))
            .withBean(KeycloakConfigData.class, () -> new KeycloakConfigData());

    @Test
    @DisplayName("Senaryo 1: Reset ayarları YOKKEN (veya false iken) temizlik bean'leri yüklenmemelidir.")
    void shouldNotLoadResetBeans_WhenPropertiesAreMissingOrFalse() {
        this.contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean("cleanMigrateStrategy");
            assertThat(context).doesNotHaveBean("keycloakCleaner");
            assertThat(context).hasBean("identityDomainService"); // Her zaman yüklenmeli
        });

        this.contextRunner
                .withPropertyValues("identity-service.init.reset-db=false", "identity-service.init.reset-keycloak=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("cleanMigrateStrategy");
                    assertThat(context).doesNotHaveBean("keycloakCleaner");
                });
    }

    @Test
    @DisplayName("Senaryo 2: Reset ayarları TRUE yapıldığında temizlik bean'leri (Flyway ve Keycloak için) yüklenmelidir.")
    void shouldLoadResetBeans_WhenPropertiesAreTrue() {
        this.contextRunner
                .withPropertyValues("identity-service.init.reset-db=true", "identity-service.init.reset-keycloak=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(FlywayMigrationStrategy.class);
                    assertThat(context).hasBean("cleanMigrateStrategy");
                    assertThat(context).hasBean("keycloakCleaner");
                });
    }

    @Test
    @DisplayName("Senaryo 3: Ayarlar bağımsız çalışabilmelidir (Biri true, diğeri false).")
    void shouldLoadBeansIndependently() {
        this.contextRunner
                .withPropertyValues("identity-service.init.reset-db=true", "identity-service.init.reset-keycloak=false")
                .run(context -> {
                    assertThat(context).hasBean("cleanMigrateStrategy");
                    assertThat(context).doesNotHaveBean("keycloakCleaner");
                });
    }
}
