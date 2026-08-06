package com.berkay.order.service.application.security;

import com.berkay.order.service.domain.ports.output.security.AuthenticationServicePort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticationServiceAdapter implements AuthenticationServicePort {

    @Override
    public UUID getCurrentUserId() {
        return UUID.fromString(getAttributes().getSubject());
    }

    @Override
    public String getCurrentUserName() {
        // Keycloak'ta genelde "preferred_username" veya "name" olur. Token yapına göre değiştir.
        return getAttributes().getClaimAsString("preferred_username");
    }

    private Jwt getAttributes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new RuntimeException("User is not authenticated via JWT!");
        }
        return (Jwt) authentication.getPrincipal();
    }
}