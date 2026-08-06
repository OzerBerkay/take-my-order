package com.berkay.identity.service.dataaccess.outbox.adapter;

import com.berkay.identity.service.dataaccess.outbox.mapper.RoleOutboxDataAccessMapper;
import com.berkay.identity.service.dataaccess.outbox.repository.RoleOutboxJpaRepository;
import com.berkay.identity.service.outbox.model.role.RoleOutboxMessage;
import com.berkay.identity.service.ports.output.repository.RoleOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleOutboxRepositoryImpl implements RoleOutboxRepository {

    private final RoleOutboxJpaRepository roleOutboxJpaRepository;
    private final RoleOutboxDataAccessMapper roleOutboxDataAccessMapper;

    @Override
    public RoleOutboxMessage save(RoleOutboxMessage roleOutboxMessage) {
        return roleOutboxDataAccessMapper.roleOutboxEntityToOutboxMessage(
                roleOutboxJpaRepository.save(roleOutboxDataAccessMapper.roleOutboxMessageToOutboxEntity(roleOutboxMessage))
        );
    }

    @Override
    public List<RoleOutboxMessage> findByOutboxStatus(OutboxStatus outboxStatus) {
        return roleOutboxJpaRepository
                .findByOutboxStatusForUpdateSkipLocked(outboxStatus.name())
                .stream()
                .map(roleOutboxDataAccessMapper::roleOutboxEntityToOutboxMessage)
                .toList();
    }

    @Override
    public void deleteByOutboxStatus(OutboxStatus outboxStatus) {
        roleOutboxJpaRepository.deleteByOutboxStatus(outboxStatus);
    }
}