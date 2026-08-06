package com.berkay.identity.service.dataaccess.outbox.repository;

import com.berkay.identity.service.dataaccess.outbox.entity.RoleOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleOutboxJpaRepository extends JpaRepository<RoleOutboxEntity, UUID> {

    @Query(value = "SELECT * FROM role_outbox WHERE outbox_status = :status ORDER BY created_at ASC FOR UPDATE SKIP LOCKED LIMIT 1", nativeQuery = true)
    Optional<RoleOutboxEntity> findNextOutboxMessageWithLock(@Param("status") String status);
    
    List<RoleOutboxEntity> findByOutboxStatusOrderByCreatedAtAsc(String status);
    @Query(value = "SELECT * FROM role_outbox WHERE outbox_status = :outboxStatus ORDER BY created_at ASC FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<RoleOutboxEntity> findByOutboxStatusForUpdateSkipLocked(@Param("outboxStatus") String outboxStatus);
    void deleteByOutboxStatus(com.berkay.outbox.OutboxStatus status);
}