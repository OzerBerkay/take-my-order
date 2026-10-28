package com.berkay.order.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

import java.util.UUID;

public class OrderReviewId extends BaseId<UUID> {
    public OrderReviewId(UUID value) {
        super(value);
    }

    @Override
    public UUID getValue() {
        return super.getValue();
    }
}
