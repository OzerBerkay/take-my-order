package com.berkay.order.service.domain.ports.input.message.listener.customer;

import com.berkay.order.service.domain.dto.message.CustomerModel;

public interface CustomerMessageListener {

    void customerCreated(CustomerModel customerModel);
}
