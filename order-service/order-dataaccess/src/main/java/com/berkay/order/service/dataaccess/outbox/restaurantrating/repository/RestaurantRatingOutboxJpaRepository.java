package com.berkay.order.service.dataaccess.outbox.restaurantrating.repository;

import com.berkay.order.service.dataaccess.outbox.restaurantrating.entity.RestaurantRatingOutboxEntity;
import com.berkay.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRatingOutboxJpaRepository extends JpaRepository<RestaurantRatingOutboxEntity, UUID> {

    Optional<List<RestaurantRatingOutboxEntity>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);

    Optional<RestaurantRatingOutboxEntity> findByTypeAndRestaurantIdAndOutboxStatus(String type, UUID restaurantId, OutboxStatus outboxStatus);

    void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);
}
