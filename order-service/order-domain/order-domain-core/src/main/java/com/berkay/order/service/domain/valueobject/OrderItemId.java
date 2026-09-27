package com.berkay.order.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

public class OrderItemId extends BaseId<Long>{
    public OrderItemId(Long value) {
        super(value);
    }
}
