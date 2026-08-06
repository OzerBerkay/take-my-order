package com.berkay.identity.service.dataaccess.outbox.adapter;

import com.berkay.identity.service.dataaccess.outbox.entity.PermissionOutboxEntity;
import com.berkay.identity.service.dataaccess.outbox.repository.PermissionOutboxJpaRepository;
import com.berkay.identity.service.outbox.model.permission.PermissionOutboxMessage;
import com.berkay.identity.service.ports.output.repository.PermissionOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PermissionOutboxRepositoryImpl implements PermissionOutboxRepository {

    private final PermissionOutboxJpaRepository permissionOutboxJpaRepository;

    public PermissionOutboxRepositoryImpl(PermissionOutboxJpaRepository permissionOutboxJpaRepository) {
        this.permissionOutboxJpaRepository = permissionOutboxJpaRepository;
    }

    @Override
    public PermissionOutboxMessage save(PermissionOutboxMessage permissionOutboxMessage) {
        PermissionOutboxEntity entity = PermissionOutboxEntity.builder()
                .id(permissionOutboxMessage.getId())
                .createdAt(permissionOutboxMessage.getCreatedAt())
                .processedAt(permissionOutboxMessage.getProcessedAt())
                .type(permissionOutboxMessage.getType())
                .payload(permissionOutboxMessage.getPayload())
                .outboxStatus(permissionOutboxMessage.getOutboxStatus())
                .version(permissionOutboxMessage.getVersion())
                .build();
        
        PermissionOutboxEntity savedEntity = permissionOutboxJpaRepository.save(entity);
        return mapToMessage(savedEntity);
    }

    @Override
    public Optional<List<PermissionOutboxMessage>> findByOutboxStatus(OutboxStatus status) {
        return permissionOutboxJpaRepository.findByOutboxStatus(status)
                .map(entities -> entities.stream()
                        .map(this::mapToMessage)
                        .collect(Collectors.toList()));
    }

    @Override
    public void deleteByOutboxStatus(OutboxStatus status) {
        permissionOutboxJpaRepository.deleteByOutboxStatus(status);
    }

    private PermissionOutboxMessage mapToMessage(PermissionOutboxEntity entity) {
        return PermissionOutboxMessage.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .processedAt(entity.getProcessedAt())
                .type(entity.getType())
                .payload(entity.getPayload())
                .outboxStatus(entity.getOutboxStatus())
                .version(entity.getVersion())
                .build();
    }
}
