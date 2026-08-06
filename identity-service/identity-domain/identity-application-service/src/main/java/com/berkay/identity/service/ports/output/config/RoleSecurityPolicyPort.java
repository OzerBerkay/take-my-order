package com.berkay.identity.service.ports.output.config;

import com.berkay.identity.service.domain.valueobject.DomainType;
import com.berkay.identity.service.domain.valueobject.UserType;
import java.util.List;

// application.yml içerisindeki security.rbac.domain-policies ayarlarını okur
public interface RoleSecurityPolicyPort {
    List<DomainType> getAllowedDomainsForUserType(UserType userType);
}