package com.berkay.customer.service.outbox.scheduler;

import com.berkay.customer.service.outbox.model.CustomerEventPayload;
import com.berkay.customer.service.outbox.model.CustomerOutboxMessage;
import com.berkay.customer.service.ports.output.repository.CustomerOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class CustomerOutboxHelper { //Repository ve ObjectMapper'ı yöneten yer burası.

    private final CustomerOutboxRepository customerOutboxRepository;
    private final ObjectMapper objectMapper;

    public CustomerOutboxHelper(CustomerOutboxRepository customerOutboxRepository, ObjectMapper objectMapper) {
        this.customerOutboxRepository = customerOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<List<CustomerOutboxMessage>> getCustomerOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        return customerOutboxRepository.findByTypeAndOutboxStatus("CUSTOMER_CREATED", outboxStatus);
    }

    @Transactional
    public void deleteCustomerOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        customerOutboxRepository.deleteByTypeAndOutboxStatus("CUSTOMER_CREATED", outboxStatus);
    }

    // Handler'ın çağıracağı ana metod
    @Transactional
    public void saveCustomerOutboxMessage(CustomerEventPayload customerEventPayload,
                                          OutboxStatus outboxStatus) {
        save(CustomerOutboxMessage.builder()
                .id(UUID.randomUUID())
                .createdAt(customerEventPayload.getCreatedAt())
                .type("CUSTOMER_CREATED")
                .payload(createPayload(customerEventPayload))
                .outboxStatus(outboxStatus)
                .build());
    }

    // Scheduler'ın Callback olarak çağıracağı metod
    @Transactional
    public void updateOutboxMessage(CustomerOutboxMessage customerOutboxMessage, OutboxStatus outboxStatus) {
        customerOutboxMessage.setOutboxStatus(outboxStatus);
        save(customerOutboxMessage);
        log.info("Customer outbox table status is updated as: {}", outboxStatus.name());
    }

    private String createPayload(CustomerEventPayload customerEventPayload) {
        try {
            return objectMapper.writeValueAsString(customerEventPayload);
        } catch (JsonProcessingException e) {
            log.error("Could not create CustomerEventPayload json!", e);
            throw new RuntimeException("Could not create CustomerEventPayload json!", e);
        }
    }

    private void save(CustomerOutboxMessage customerOutboxMessage) {
        CustomerOutboxMessage response = customerOutboxRepository.save(customerOutboxMessage);
        if (response == null) {
            log.error("Could not save CustomerOutboxMessage!");
            throw new RuntimeException("Could not save CustomerOutboxMessage!");
        }
        log.info("CustomerOutboxMessage is saved with id: {}", customerOutboxMessage.getId());
    }
}
