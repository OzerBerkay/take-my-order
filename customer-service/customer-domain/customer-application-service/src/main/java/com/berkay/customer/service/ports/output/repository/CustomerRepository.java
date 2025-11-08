package com.berkay.customer.service.ports.output.repository;

import com.berkay.customer.service.domain.entity.Customer;

public interface CustomerRepository {

    Customer createCustomer(Customer customer);
}
