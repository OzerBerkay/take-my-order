package com.berkay.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    // Mocking ReactiveJwtDecoder avoids trying to reach Keycloak during the test
    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Test
    void shouldPermitAuthLogin() {
        webTestClient.post().uri("/auth/login")
                .exchange()
                .expectStatus().value(status -> org.junit.jupiter.api.Assertions.assertNotEquals(401, status));
    }

    @Test
    void shouldPermitAuthRefresh() {
        webTestClient.post().uri("/auth/refresh")
                .exchange()
                .expectStatus().value(status -> org.junit.jupiter.api.Assertions.assertNotEquals(401, status));
    }

    @Test
    void shouldPermitActuator() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk(); // Actuator health is typically 200 OK
    }

    @Test
    void shouldPermitPublicEndpoints() {
        webTestClient.get().uri("/public/restaurants")
                .exchange()
                .expectStatus().value(status -> org.junit.jupiter.api.Assertions.assertNotEquals(401, status));
    }

    @Test
    void shouldBlockProtectedEndpoints() {
        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isUnauthorized(); // Blocked by SecurityConfig
    }

    @Test
    void shouldAllowCorsForCustomerPort4200() {
        webTestClient.options().uri("/auth/login")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:4200")
                .expectHeader().valueEquals("Access-Control-Allow-Credentials", "true");
    }

    @Test
    void shouldAllowCorsForMerchantPort4201() {
        webTestClient.options().uri("/auth/login")
                .header("Origin", "http://localhost:4201")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:4201")
                .expectHeader().valueEquals("Access-Control-Allow-Credentials", "true");
    }

    @Test
    void shouldAllowCorsForInternalPort4202() {
        webTestClient.options().uri("/auth/login")
                .header("Origin", "http://localhost:4202")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:4202")
                .expectHeader().valueEquals("Access-Control-Allow-Credentials", "true");
    }

    @Test
    void shouldRejectCorsForUnauthorizedOrigin() {
        webTestClient.options().uri("/auth/login")
                .header("Origin", "http://localhost:9999")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectHeader().doesNotExist("Access-Control-Allow-Origin");
    }
}
