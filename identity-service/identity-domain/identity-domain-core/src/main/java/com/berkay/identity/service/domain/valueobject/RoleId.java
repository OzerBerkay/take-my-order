package com.berkay.identity.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

import java.util.UUID;

public class RoleId extends BaseId<UUID> {
    public RoleId(UUID value) {
        super(value);
    }
}
