package com.berkay.payment.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

import java.util.UUID;

public class OrderEscrowId extends BaseId<UUID> {
    public OrderEscrowId(UUID value) {
        super(value);
    }
}
