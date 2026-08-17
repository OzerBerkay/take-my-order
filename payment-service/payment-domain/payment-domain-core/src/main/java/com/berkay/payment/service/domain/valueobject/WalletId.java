package com.berkay.payment.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

import java.util.UUID;

public class WalletId extends BaseId<UUID> {
    public WalletId(UUID value) {
        super(value);
    }
}
