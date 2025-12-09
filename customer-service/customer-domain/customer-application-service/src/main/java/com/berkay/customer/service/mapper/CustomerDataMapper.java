package com.berkay.customer.service.mapper;

import com.berkay.customer.service.create.CreateCustomerCommand;
import com.berkay.customer.service.create.CreateCustomerResponse;
import com.berkay.customer.service.domain.entity.Customer;
import com.berkay.domain.valueobject.CustomerId;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataMapper {

    public Customer createCustomerCommandToCustomer(CreateCustomerCommand createCustomerCommand) {
        return new Customer(new CustomerId(createCustomerCommand.getCustomerId()),
                createCustomerCommand.getUsername(),
                createCustomerCommand.getFirstName(),
                createCustomerCommand.getLastName(),
                new CustomerEmail(createCustomerCommand.getEmail()));
    }

    public CreateCustomerResponse customerToCreateCustomerResponse(Customer customer, String message) {
        return new CreateCustomerResponse(customer.getId().getValue(), message);
    }
}
