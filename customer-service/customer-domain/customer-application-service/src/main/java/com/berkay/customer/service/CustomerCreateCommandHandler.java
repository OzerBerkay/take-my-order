package com.berkay.customer.service;

import com.berkay.customer.service.create.CreateCustomerCommand;
import com.berkay.customer.service.domain.CustomerDomainService;
import com.berkay.customer.service.domain.entity.Customer;
import com.berkay.customer.service.domain.event.CustomerCreatedEvent;
import com.berkay.customer.service.domain.exception.CustomerDomainException;
import com.berkay.customer.service.mapper.CustomerDataMapper;
import com.berkay.customer.service.outbox.model.CustomerEventPayload;
import com.berkay.customer.service.outbox.scheduler.CustomerOutboxHelper;
import com.berkay.customer.service.ports.output.repository.CustomerRepository;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
class CustomerCreateCommandHandler {

    private final CustomerDomainService customerDomainService;
    private final CustomerRepository customerRepository;
    private final CustomerDataMapper customerDataMapper;

    // DEĞİŞİKLİK 1: Repository GİTTİ, Helper GELDİ
    // private final CustomerOutboxRepository customerOutboxRepository; <-- SİL
    private final CustomerOutboxHelper customerOutboxHelper; // <-- EKLE

    public CustomerCreateCommandHandler(CustomerDomainService customerDomainService,
                                        CustomerRepository customerRepository,
                                        CustomerDataMapper customerDataMapper,
                                        CustomerOutboxHelper customerOutboxHelper) { // Constructor'ı güncelle
        this.customerDomainService = customerDomainService;
        this.customerRepository = customerRepository;
        this.customerDataMapper = customerDataMapper;
        this.customerOutboxHelper = customerOutboxHelper;
    }

    @Transactional
    public CustomerCreatedEvent createCustomer(CreateCustomerCommand createCustomerCommand) {
        Optional<Customer> existingCustomer = customerRepository.findCustomerByEmail(createCustomerCommand.getEmail());
        if (existingCustomer.isPresent()) {
            log.warn("Could not create customer. Email already exists: {}", createCustomerCommand.getEmail());
            throw new CustomerDomainException("Could not create customer. Email already exists: " + createCustomerCommand.getEmail());
        }

        Customer customer = customerDataMapper.createCustomerCommandToCustomer(createCustomerCommand);
        CustomerCreatedEvent customerCreatedEvent = customerDomainService.validateAndInitiateCustomer(customer);

        Customer savedCustomer = customerRepository.createCustomer(customer);
        if (savedCustomer == null) {
            log.error("Could not save customer with id: {}", createCustomerCommand.getCustomerId());
            throw new CustomerDomainException("Could not save customer with id " +
                    createCustomerCommand.getCustomerId());
        }

        // Artık Mapper'ı ve Repository'i çağırmıyoruz.
        // Kirli işlerin hepsini Helper'a devrettik.
        log.info("Saving Outbox Message for customer id: {}", createCustomerCommand.getCustomerId());

        // 1. Önce Event'i Payload DTO'suna çeviriyoruz
        CustomerEventPayload customerEventPayload = customerDataMapper.customerCreatedEventToCustomerEventPayload(customerCreatedEvent);

        // 2. Helper'a Payload veriyoruz (ve başlangıç statüsünü STARTED olarak belirtiyoruz)
        customerOutboxHelper.saveCustomerOutboxMessage(customerEventPayload, OutboxStatus.STARTED);

        log.info("Returning CustomerCreatedEvent for customer id: {}", createCustomerCommand.getCustomerId());
        return customerCreatedEvent;
    }
}
