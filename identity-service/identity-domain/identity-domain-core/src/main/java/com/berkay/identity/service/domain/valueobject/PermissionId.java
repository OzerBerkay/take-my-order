package com.berkay.identity.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

import java.util.UUID;

public class PermissionId extends BaseId<UUID> {
    public PermissionId(UUID value) {
        super(value);
    }
}
