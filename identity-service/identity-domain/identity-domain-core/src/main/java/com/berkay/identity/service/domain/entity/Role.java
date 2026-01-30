package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.identity.service.domain.valueobject.RoleId;

import java.util.List;

public class Role extends BaseEntity<RoleId> {
    private final String name;
    private final List<Permission> permissions;

    public Role(RoleId roleId, String name, List<Permission> permissions) {
        super.setId(roleId);
        this.name = name;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }
}
