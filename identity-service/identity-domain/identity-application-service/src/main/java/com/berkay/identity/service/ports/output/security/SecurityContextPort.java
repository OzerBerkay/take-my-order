package com.berkay.identity.service.ports.output.security;

import com.berkay.identity.service.domain.valueobject.UserType;
import java.util.UUID;

// İsteği atan kişinin JWT token'ından okunan değerleri verir
public interface SecurityContextPort {
    UUID getCurrentInternalUserId();
    UUID getCurrentExternalUserId();
    UserType getCurrentUserType();
    java.util.List<UUID> getCurrentUserRoleIds();
    java.util.Set<UUID> getAllowedOrganizationalUnitIds();
    String getCurrentSessionId();
}