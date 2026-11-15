package com.berkay.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "SWAGGER_SUPPORTED_SUBMIT_METHODS="
)
class SwaggerReadOnlyConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Test
    void shouldDisableAllSubmitMethodsWhenConfiguredEmptyForProdReadOnly() {
        webTestClient.get().uri("/v3/api-docs/swagger-config")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.supportedSubmitMethods").isArray()
                .jsonPath("$.supportedSubmitMethods").isEmpty();
    }
}
