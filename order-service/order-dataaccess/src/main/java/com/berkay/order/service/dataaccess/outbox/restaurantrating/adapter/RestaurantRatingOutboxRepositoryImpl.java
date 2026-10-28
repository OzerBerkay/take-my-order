package com.berkay.order.service.dataaccess.outbox.restaurantrating.adapter;

import com.berkay.order.service.dataaccess.outbox.restaurantrating.entity.RestaurantRatingOutboxEntity;
import com.berkay.order.service.dataaccess.outbox.restaurantrating.mapper.RestaurantRatingOutboxDataAccessMapper;
import com.berkay.order.service.dataaccess.outbox.restaurantrating.repository.RestaurantRatingOutboxJpaRepository;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRatingOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantRatingOutboxRepositoryImpl implements RestaurantRatingOutboxRepository {

    private final RestaurantRatingOutboxJpaRepository repository;
    private final RestaurantRatingOutboxDataAccessMapper mapper;

    public RestaurantRatingOutboxRepositoryImpl(RestaurantRatingOutboxJpaRepository repository,
                                                RestaurantRatingOutboxDataAccessMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public RestaurantRatingOutboxMessage save(RestaurantRatingOutboxMessage message) {
        Optional<RestaurantRatingOutboxEntity> existing = repository.findById(message.getId());
        RestaurantRatingOutboxEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setProcessedAt(message.getProcessedAt());
            entity.setOutboxStatus(message.getOutboxStatus());
            entity.setPayload(message.getPayload());
            entity.setCreatedAt(message.getCreatedAt());
        } else {
            entity = mapper.restaurantRatingOutboxMessageToRestaurantRatingOutboxEntity(message);
        }
        return mapper.restaurantRatingOutboxEntityToRestaurantRatingOutboxMessage(
                repository.save(entity));
    }

    @Override
    public Optional<List<RestaurantRatingOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
        return repository.findByTypeAndOutboxStatus(type, outboxStatus)
                .map(list -> list.stream()
                        .map(mapper::restaurantRatingOutboxEntityToRestaurantRatingOutboxMessage)
                        .collect(Collectors.toList()));
    }

    @Override
    public Optional<RestaurantRatingOutboxMessage> findByTypeAndRestaurantIdAndOutboxStatus(String type, UUID restaurantId, OutboxStatus outboxStatus) {
        return repository.findByTypeAndRestaurantIdAndOutboxStatus(type, restaurantId, outboxStatus)
                .map(mapper::restaurantRatingOutboxEntityToRestaurantRatingOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
        repository.deleteByTypeAndOutboxStatus(type, outboxStatus);
    }
}
