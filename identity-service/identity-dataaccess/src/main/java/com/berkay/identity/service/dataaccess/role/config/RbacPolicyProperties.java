package com.berkay.identity.service.dataaccess.role.config;

import com.berkay.identity.service.domain.valueobject.DomainType;
import com.berkay.identity.service.domain.valueobject.UserType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.rbac")
public class RbacPolicyProperties {
    // YAML'daki "MERCHANT: RESTAURANT" gibi listeleri Map olarak alır.
    private Map<UserType, List<DomainType>> domainPolicies;
}