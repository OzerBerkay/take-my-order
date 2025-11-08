package com.berkay.customer.service.ports.input.service;

import com.berkay.customer.service.create.CreateCustomerCommand;
import com.berkay.customer.service.create.CreateCustomerResponse;
import jakarta.validation.Valid;

public interface CustomerApplicationService {

    CreateCustomerResponse createCustomer(@Valid CreateCustomerCommand createCustomerCommand);

}
