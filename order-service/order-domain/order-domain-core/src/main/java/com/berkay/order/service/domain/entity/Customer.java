package com.berkay.order.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.CustomerId;

public class Customer extends AggregateRoot<CustomerId> {
    public Customer(){

    }

    public Customer(CustomerId customerId) {
        super.setId(customerId);
    }
}
