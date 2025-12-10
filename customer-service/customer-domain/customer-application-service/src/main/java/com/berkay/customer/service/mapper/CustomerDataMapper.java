package com.berkay.customer.service.mapper;

import com.berkay.customer.service.create.CreateCustomerCommand;
import com.berkay.customer.service.create.CreateCustomerResponse;
import com.berkay.customer.service.domain.entity.Customer;
import com.berkay.customer.service.domain.event.CustomerCreatedEvent;
import com.berkay.customer.service.domain.valueobject.CustomerEmail;
import com.berkay.customer.service.outbox.model.CustomerEventPayload;
import com.berkay.domain.valueobject.CustomerId;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomerDataMapper {

    public Customer createCustomerCommandToCustomer(CreateCustomerCommand createCustomerCommand, String keycloakUserId) {
        return new Customer(new CustomerId(UUID.fromString(keycloakUserId)),
                createCustomerCommand.getUsername(),
                createCustomerCommand.getFirstName(),
                createCustomerCommand.getLastName(),
                new CustomerEmail(createCustomerCommand.getEmail()));
    }

    public CreateCustomerResponse customerToCreateCustomerResponse(Customer customer, String message) {
        return new CreateCustomerResponse(customer.getId().getValue(), message);
    }

    public CustomerEventPayload customerCreatedEventToCustomerEventPayload(CustomerCreatedEvent customerCreatedEvent) {
        return CustomerEventPayload.builder()
                .customerId(customerCreatedEvent.getCustomer().getId().getValue().toString())
                .username(customerCreatedEvent.getCustomer().getUsername())
                .firstName(customerCreatedEvent.getCustomer().getFirstName())
                .lastName(customerCreatedEvent.getCustomer().getLastName())
                .email(customerCreatedEvent.getCustomer().getEmail().getValue())
                .createdAt(customerCreatedEvent.getCreatedAt())
                .build();
    }
}
