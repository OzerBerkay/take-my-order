package com.berkay.customer.service.ports.output.repository;

import com.berkay.customer.service.domain.entity.Customer;

import java.util.Optional;

public interface CustomerRepository {

    Customer createCustomer(Customer customer);
    Optional<Customer> findCustomerByEmail(String email);
}
