package com.berkay.identity.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

import java.util.UUID;

public class OrganizationalUnitId extends BaseId<UUID> {
    public OrganizationalUnitId(UUID value) {
        super(value);
    }
}
