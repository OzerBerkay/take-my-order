package com.berkay.customer.service.domain;

import com.berkay.customer.service.domain.entity.Customer;
import com.berkay.customer.service.domain.event.CustomerCreatedEvent;

public interface CustomerDomainService {

    CustomerCreatedEvent validateAndInitiateCustomer(Customer customer);

}
