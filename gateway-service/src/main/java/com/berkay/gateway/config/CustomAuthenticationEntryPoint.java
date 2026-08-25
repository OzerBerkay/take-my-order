package com.berkay.gateway.config;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorCode = "UNAUTHORIZED";
        String message = "Authentication failed or token is missing/invalid";

        // Check if the exception indicates an expired or invalid token
        if (ex instanceof InvalidBearerTokenException) {
            String exMessage = ex.getMessage();
            if (exMessage != null && exMessage.toLowerCase().contains("expired")) {
                errorCode = "ACCESS_TOKEN_EXPIRED";
                message = "Token is expired. Please log in again or refresh your token.";
            } else {
                errorCode = "INVALID_TOKEN";
                message = "The provided token is invalid.";
            }
        }

        String jsonResponse = String.format(
                "{\"status\": %d, \"error_code\": \"%s\", \"message\": \"%s\"}",
                HttpStatus.UNAUTHORIZED.value(), errorCode, message
        );

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
