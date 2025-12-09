package com.berkay.customer.service.domain.entity;

import com.berkay.customer.service.domain.valueobject.CustomerEmail;
import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.CustomerId;

public class Customer extends AggregateRoot<CustomerId> {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final CustomerEmail email;

    public Customer(CustomerId customerId, String username, String firstName, String lastName, CustomerEmail email) {
        this.email = email;
        super.setId(customerId);
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public CustomerEmail getEmail() {return email;}
}
