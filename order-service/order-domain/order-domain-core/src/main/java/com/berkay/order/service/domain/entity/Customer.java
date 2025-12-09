package com.berkay.order.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.CustomerId;

public class Customer extends AggregateRoot<CustomerId> {

    private String username;
    private String firstName;
    private String lastName;
    private String email;

    public Customer(CustomerId customerId,
                    String username,
                    String firstName,
                    String lastName,
                    String email) {
        super.setId(customerId);
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Customer(CustomerId customerId) {
        super.setId(customerId);
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

    public String getEmail() { return email; }
}
