package com.berkay.customer.service.dataaccess.outbox.repository;

import com.berkay.customer.service.dataaccess.outbox.entity.CustomerOutboxEntity;
import com.berkay.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerOutboxJpaRepository extends JpaRepository<CustomerOutboxEntity, UUID> {

    // Scheduler için: Belirli bir türedeki ve durumdaki (Örn: STARTED) mesajları getir.
    Optional<List<CustomerOutboxEntity>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);

    // Temizlik için: İşlenmiş mesajları sil.
    void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);
}
