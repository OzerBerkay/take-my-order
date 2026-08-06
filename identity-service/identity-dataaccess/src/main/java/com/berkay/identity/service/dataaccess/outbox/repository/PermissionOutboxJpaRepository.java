package com.berkay.identity.service.dataaccess.outbox.repository;

import com.berkay.identity.service.dataaccess.outbox.entity.PermissionOutboxEntity;
import com.berkay.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionOutboxJpaRepository extends JpaRepository<PermissionOutboxEntity, UUID> {
    Optional<List<PermissionOutboxEntity>> findByOutboxStatus(OutboxStatus status);
    void deleteByOutboxStatus(OutboxStatus status);
}
