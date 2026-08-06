package com.berkay.identity.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

import java.util.UUID;

public class IntentId extends BaseId<UUID> {
    public IntentId(UUID value) {
        super(value);
    }
}
