package com.berkay.identity.service.dataaccess.outbox.repository;

import com.berkay.identity.service.dataaccess.outbox.entity.UserOutboxEntity;
import com.berkay.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserOutboxJpaRepository extends JpaRepository<UserOutboxEntity, UUID> {

    List<UserOutboxEntity> findByOutboxStatus(OutboxStatus outboxStatus);

    void deleteByOutboxStatus(OutboxStatus outboxStatus);
}