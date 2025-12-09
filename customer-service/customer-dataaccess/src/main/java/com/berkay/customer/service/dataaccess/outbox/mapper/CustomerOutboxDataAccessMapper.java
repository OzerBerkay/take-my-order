package com.berkay.customer.service.dataaccess.outbox.mapper;

import com.berkay.customer.service.dataaccess.outbox.entity.CustomerOutboxEntity;
import com.berkay.customer.service.outbox.model.CustomerOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class CustomerOutboxDataAccessMapper {

    // Domain -> Entity (Kaydederken kullanacağız)
    public CustomerOutboxEntity customerOutboxMessageToCustomerOutboxEntity(CustomerOutboxMessage customerOutboxMessage) {
        return CustomerOutboxEntity.builder()
                .id(customerOutboxMessage.getId())
                .createdAt(customerOutboxMessage.getCreatedAt())
                .processedAt(customerOutboxMessage.getProcessedAt())
                .type(customerOutboxMessage.getType())
                .payload(customerOutboxMessage.getPayload())
                .outboxStatus(customerOutboxMessage.getOutboxStatus())
                .version(customerOutboxMessage.getVersion())
                .build();
    }

    // Entity -> Domain (Veritabanından okurken kullanacağız)
    public CustomerOutboxMessage customerOutboxEntityToCustomerOutboxMessage(CustomerOutboxEntity customerOutboxEntity) {
        return CustomerOutboxMessage.builder()
                .id(customerOutboxEntity.getId())
                .createdAt(customerOutboxEntity.getCreatedAt())
                .processedAt(customerOutboxEntity.getProcessedAt())
                .type(customerOutboxEntity.getType())
                .payload(customerOutboxEntity.getPayload())
                .outboxStatus(customerOutboxEntity.getOutboxStatus())
                .version(customerOutboxEntity.getVersion())
                .build();
    }
}
