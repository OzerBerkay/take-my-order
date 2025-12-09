package com.berkay.customer.service.dataaccess.customer.mapper;

import com.berkay.customer.service.dataaccess.customer.entity.CustomerEntity;
import com.berkay.customer.service.domain.entity.Customer;
import com.berkay.customer.service.domain.valueobject.CustomerEmail;
import com.berkay.domain.valueobject.CustomerId;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAccessMapper {

    public Customer customerEntityToCustomer(CustomerEntity customerEntity) {
        return new Customer(new CustomerId(customerEntity.getId()),
                customerEntity.getUsername(),
                customerEntity.getFirstName(),
                customerEntity.getLastName(),
                new CustomerEmail(customerEntity.getEmail()));
    }

    public CustomerEntity customerToCustomerEntity(Customer customer) {
        return CustomerEntity.builder()
                .id(customer.getId().getValue())
                .username(customer.getUsername())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail().getValue())
                .build();
    }

}