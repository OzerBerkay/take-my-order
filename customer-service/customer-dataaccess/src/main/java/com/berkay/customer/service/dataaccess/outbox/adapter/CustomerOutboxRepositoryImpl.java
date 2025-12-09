package com.berkay.customer.service.dataaccess.outbox.adapter;

import com.berkay.customer.service.dataaccess.outbox.entity.CustomerOutboxEntity;
import com.berkay.customer.service.dataaccess.outbox.mapper.CustomerOutboxDataAccessMapper;
import com.berkay.customer.service.dataaccess.outbox.repository.CustomerOutboxJpaRepository;
import com.berkay.customer.service.outbox.model.CustomerOutboxMessage;
import com.berkay.customer.service.ports.output.repository.CustomerOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CustomerOutboxRepositoryImpl implements CustomerOutboxRepository {

    private final CustomerOutboxJpaRepository customerOutboxJpaRepository;
    private final CustomerOutboxDataAccessMapper customerOutboxDataAccessMapper;

    public CustomerOutboxRepositoryImpl(CustomerOutboxJpaRepository customerOutboxJpaRepository,
                                        CustomerOutboxDataAccessMapper customerOutboxDataAccessMapper) {
        this.customerOutboxJpaRepository = customerOutboxJpaRepository;
        this.customerOutboxDataAccessMapper = customerOutboxDataAccessMapper;
    }

    @Override
    public CustomerOutboxMessage save(CustomerOutboxMessage customerOutboxMessage) {
        // 1. Domain modelini Entity'e çevir
        CustomerOutboxEntity entity = customerOutboxDataAccessMapper
                .customerOutboxMessageToCustomerOutboxEntity(customerOutboxMessage);

        // 2. JPA ile veritabanına kaydet
        CustomerOutboxEntity savedEntity = customerOutboxJpaRepository.save(entity);

        // 3. Kaydedilen Entity'i tekrar Domain modeline çevir ve geri dön
        return customerOutboxDataAccessMapper.customerOutboxEntityToCustomerOutboxMessage(savedEntity);
    }

    @Override
    public Optional<List<CustomerOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
        return customerOutboxJpaRepository.findByTypeAndOutboxStatus(type, outboxStatus)
                .map(entities -> entities.stream()
                        .map(customerOutboxDataAccessMapper::customerOutboxEntityToCustomerOutboxMessage)
                        .collect(Collectors.toList()));
    }

    @Override
    public void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
        customerOutboxJpaRepository.deleteByTypeAndOutboxStatus(type, outboxStatus);
    }
}