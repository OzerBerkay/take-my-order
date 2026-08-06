package com.berkay.identity.service.domain;

import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.event.*;
import com.berkay.identity.service.domain.valueobject.DomainType;

import java.util.List;

public interface IdentityDomainService {
    RoleCreatedEvent validateAndInitiateRoleCreate(Role role, List<Permission> callerPermissions, List<DomainType> callerAllowedDomains);

    RoleUpdatedEvent validateAndInitiateRoleUpdate(Role role, String newName, List<Permission> newPermissions, List<Permission> callerPermissions, List<DomainType> callerAllowedDomains);

    RoleDeletedEvent validateAndInitiateRoleDelete(Role role);

    void initiateCustomer(User user);
    void initiateMerchant(User user);
    void initiateInternalUser(User user);

    void validateAndInitiatePermissionUpdate(Permission permission, String newDescription, boolean active);
}