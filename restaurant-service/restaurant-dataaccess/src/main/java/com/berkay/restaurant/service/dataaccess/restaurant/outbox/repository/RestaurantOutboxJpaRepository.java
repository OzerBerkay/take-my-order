package com.berkay.restaurant.service.dataaccess.restaurant.outbox.repository;

import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.dataaccess.restaurant.outbox.entity.RestaurantOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantOutboxJpaRepository extends JpaRepository<RestaurantOutboxEntity, UUID> {

    List<RestaurantOutboxEntity> findByTypeInAndOutboxStatus(List<String> types, OutboxStatus outboxStatus);

    void deleteByTypeInAndOutboxStatus(List<String> types, OutboxStatus outboxStatus);
}
