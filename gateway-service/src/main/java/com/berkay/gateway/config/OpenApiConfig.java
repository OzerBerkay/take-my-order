package com.berkay.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Take My Order - API Gateway & Cloud Microservices Hub")
                        .version("v1.0")
                        .description("### 🛡️ Enterprise Architecture & Security Note\n\n"
                                + "**Production Standard:** In enterprise commercial environments, OpenAPI/Swagger UI endpoints are typically disabled or restricted behind private VPNs to eliminate information disclosure and minimize attack surfaces.\n\n"
                                + "**Read-Only Showcase Mode:** For evaluation, portfolio demonstration, and technical leadership review, API documentation is enabled (`SWAGGER_ENABLED=true`) in read-only mode across the gateway hub—interactive execution (\"Try it out\") is disabled in production via `SWAGGER_SUPPORTED_SUBMIT_METHODS`, while still toggleable at runtime via the centralized ConfigMap (`service-config`).\n\n"
                                + "Use the top-right definition selector to inspect the individual service APIs (Identity, Order, Payment, Restaurant).")
                        .contact(new Contact().name("Berkay Özer").url("https://github.com/OzerBerkay/take-my-order")));
    }
}

