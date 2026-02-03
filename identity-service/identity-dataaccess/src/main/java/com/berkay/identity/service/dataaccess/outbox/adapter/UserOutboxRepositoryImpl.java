package com.berkay.identity.service.dataaccess.outbox.adapter;

import com.berkay.identity.service.dataaccess.outbox.entity.UserOutboxEntity;
import com.berkay.identity.service.dataaccess.outbox.mapper.UserOutboxDataAccessMapper;
import com.berkay.identity.service.dataaccess.outbox.repository.UserOutboxJpaRepository;
import com.berkay.identity.service.outbox.model.UserOutboxMessage;
import com.berkay.identity.service.ports.output.repository.UserOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserOutboxRepositoryImpl implements UserOutboxRepository {

    private final UserOutboxJpaRepository userOutboxJpaRepository;
    private final UserOutboxDataAccessMapper userOutboxDataAccessMapper;

    @Override
    public UserOutboxMessage save(UserOutboxMessage userOutboxMessage) {
        UserOutboxEntity entity = userOutboxDataAccessMapper
                .outboxMessageToOutboxEntity(userOutboxMessage);

        UserOutboxEntity savedEntity = userOutboxJpaRepository.save(entity);

        return userOutboxDataAccessMapper.outboxEntityToOutboxMessage(savedEntity);
    }

    @Override
    public List<UserOutboxMessage> findByOutboxStatus(OutboxStatus status) {
        return userOutboxJpaRepository.findByOutboxStatus(status)
                .stream()
                .map(userOutboxDataAccessMapper::outboxEntityToOutboxMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByOutboxStatus(OutboxStatus status) {
        userOutboxJpaRepository.deleteByOutboxStatus(status);
    }
}