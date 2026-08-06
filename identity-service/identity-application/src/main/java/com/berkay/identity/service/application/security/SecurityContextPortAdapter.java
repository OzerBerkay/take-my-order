package com.berkay.identity.service.application.security;

import com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.ports.output.security.SecurityContextPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityContextPortAdapter implements SecurityContextPort {

    @Override
    public UUID getCurrentInternalUserId() {
        return getJwtToken().getInternalId();
    }

    @Override
    public UUID getCurrentExternalUserId() {
        return getJwtToken().getExternalId();
    }

    @Override
    public UserType getCurrentUserType() {
        return getJwtToken().getUserType();
    }

    @Override
    public java.util.List<UUID> getCurrentUserRoleIds() {
        return getJwtToken().getRoleIds();
    }

    @Override
    public java.util.Set<UUID> getAllowedOrganizationalUnitIds() {
        return new java.util.HashSet<>(getJwtToken().getOrganizationalUnitIds());
    }

    @Override
    public String getCurrentSessionId() {
        return getJwtToken().getSid();
    }

    private JwtAuthenticationToken getJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return (JwtAuthenticationToken) authentication;
        }
        throw new IdentityDomainException("Security context does not contain a valid JWT Token!");
    }
}