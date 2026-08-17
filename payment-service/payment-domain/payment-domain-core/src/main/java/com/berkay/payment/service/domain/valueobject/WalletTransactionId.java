package com.berkay.payment.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

import java.util.UUID;

public class WalletTransactionId extends BaseId<UUID> {
    public WalletTransactionId(UUID value) {
        super(value);
    }
}
