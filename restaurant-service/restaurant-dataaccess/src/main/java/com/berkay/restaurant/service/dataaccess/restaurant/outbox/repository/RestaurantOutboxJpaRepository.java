package com.berkay.restaurant.service.dataaccess.restaurant.outbox.repository;

import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.dataaccess.restaurant.outbox.entity.RestaurantOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantOutboxJpaRepository extends JpaRepository<RestaurantOutboxEntity, UUID> {

    List<RestaurantOutboxEntity> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);

    Optional<RestaurantOutboxEntity> findByTypeAndSagaIdAndOutboxStatus(String type, UUID sagaId, OutboxStatus outboxStatus);

    void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);
}
