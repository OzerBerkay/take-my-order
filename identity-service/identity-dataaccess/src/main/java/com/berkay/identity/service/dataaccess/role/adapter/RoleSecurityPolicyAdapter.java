package com.berkay.identity.service.dataaccess.role.adapter;

import com.berkay.identity.service.dataaccess.role.config.RbacPolicyProperties;
import com.berkay.identity.service.domain.valueobject.DomainType;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.ports.output.config.RoleSecurityPolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleSecurityPolicyAdapter implements RoleSecurityPolicyPort {

    private final RbacPolicyProperties rbacPolicyProperties;

    @Override
    public List<DomainType> getAllowedDomainsForUserType(UserType userType) {
        if (rbacPolicyProperties.getDomainPolicies() == null) {
            return Collections.emptyList();
        }
        return rbacPolicyProperties.getDomainPolicies().getOrDefault(userType, Collections.emptyList());
    }
}