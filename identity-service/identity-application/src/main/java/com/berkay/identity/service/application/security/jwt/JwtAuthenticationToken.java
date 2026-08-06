package com.berkay.identity.service.application.security.jwt;

import com.berkay.identity.service.domain.valueobject.AccountStatus;
import com.berkay.identity.service.domain.valueobject.UserType;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UUID externalId;   // "sub"
    private final UUID internalId;   // "internal_id"
    private final UserType userType;
    private final AccountStatus accountStatus;
    private final String email;
    private final List<UUID> roleIds;
    private final List<UUID> organizationalUnitIds;
    private final String sid;
    private final String token;

    public JwtAuthenticationToken(UUID externalId, UUID internalId, UserType userType,
                                  AccountStatus accountStatus, String email,
                                  List<UUID> roleIds, List<UUID> organizationalUnitIds, String sid, String token) {
        super(Collections.emptyList()); // Rolleri (GrantedAuthorities) Spring formatında değil, kendi Cache yapımızda yöneteceğiz.
        this.externalId = externalId;
        this.internalId = internalId;
        this.userType = userType;
        this.accountStatus = accountStatus;
        this.email = email;
        this.roleIds = roleIds != null ? roleIds : Collections.emptyList();
        this.organizationalUnitIds = organizationalUnitIds != null ? organizationalUnitIds : Collections.emptyList();
        this.sid = sid;
        this.token = token;
        setAuthenticated(true); // Gateway doğruladığı için direkt true kabul ediyoruz. (Gateway Sadece kaba (Coarse-grained) kontrolleri yapar. JWT'nin imzası doğru mu? Süresi dolmuş mu? Formatı geçerli mi?)
    }

    @Override
    public Object getCredentials() { return token; }

    @Override
    public Object getPrincipal() { return internalId; } // Sistemdeki asıl kimliğimiz internalId'dir.
}