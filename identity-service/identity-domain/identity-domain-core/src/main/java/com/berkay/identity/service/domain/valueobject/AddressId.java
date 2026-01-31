package com.berkay.identity.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;
import java.util.UUID;

public class AddressId extends BaseId<UUID> {
    public AddressId(UUID value) {
        super(value);
    }
}