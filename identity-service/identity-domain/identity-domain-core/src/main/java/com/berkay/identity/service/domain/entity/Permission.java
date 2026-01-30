package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.identity.service.domain.valueobject.PermissionId;

public class Permission extends BaseEntity<PermissionId> {
    private final String name;

    public Permission(PermissionId permissionId, String name) {
        super.setId(permissionId);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
