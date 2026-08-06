package com.berkay.application.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UUID internalId;
    private final String externalId;
    private final String userType;
    private final List<UUID> roleIds;
    private final List<UUID> organizationalUnitIds;
    private final String sid;

    public JwtAuthenticationToken(UUID internalId, String externalId, String userType, 
                                  List<UUID> roleIds, List<UUID> organizationalUnitIds, 
                                  String sid, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.internalId = internalId;
        this.externalId = externalId;
        this.userType = userType;
        this.roleIds = roleIds;
        this.organizationalUnitIds = organizationalUnitIds;
        this.sid = sid;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return internalId;
    }

    public UUID getInternalId() {
        return internalId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getUserType() {
        return userType;
    }

    public List<UUID> getRoleIds() {
        return roleIds;
    }

    public List<UUID> getOrganizationalUnitIds() {
        return organizationalUnitIds;
    }

    public String getSid() {
        return sid;
    }
}
