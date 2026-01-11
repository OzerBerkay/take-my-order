package com.berkay.restaurant.service.dataaccess.restaurant.outbox.adapter;

import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.dataaccess.restaurant.outbox.mapper.RestaurantOutboxDataAccessMapper;
import com.berkay.restaurant.service.dataaccess.restaurant.outbox.repository.RestaurantOutboxJpaRepository;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantOutboxRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantOutboxRepositoryImpl implements RestaurantOutboxRepository {

    private final RestaurantOutboxJpaRepository restaurantOutboxJpaRepository;
    private final RestaurantOutboxDataAccessMapper restaurantOutboxDataAccessMapper;

    public RestaurantOutboxRepositoryImpl(RestaurantOutboxJpaRepository restaurantOutboxJpaRepository,
                                          RestaurantOutboxDataAccessMapper restaurantOutboxDataAccessMapper) {
        this.restaurantOutboxJpaRepository = restaurantOutboxJpaRepository;
        this.restaurantOutboxDataAccessMapper = restaurantOutboxDataAccessMapper;
    }

    @Override
    public RestaurantOutboxMessage save(RestaurantOutboxMessage restaurantOutboxMessage) {
        return restaurantOutboxDataAccessMapper.outboxEntityToOrderOutboxMessage(
                restaurantOutboxJpaRepository.save(
                        restaurantOutboxDataAccessMapper.orderOutboxMessageToOutboxEntity(restaurantOutboxMessage)));
    }

    @Override
    public Optional<List<RestaurantOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
        return Optional.of(restaurantOutboxJpaRepository.findByTypeAndOutboxStatus(type, outboxStatus)
                .stream()
                .map(restaurantOutboxDataAccessMapper::outboxEntityToOrderOutboxMessage)
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<RestaurantOutboxMessage> findByTypeAndSagaIdAndOutboxStatus(String type, UUID sagaId, OutboxStatus outboxStatus) {
        return restaurantOutboxJpaRepository.findByTypeAndSagaIdAndOutboxStatus(type, sagaId, outboxStatus)
                .map(restaurantOutboxDataAccessMapper::outboxEntityToOrderOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
        restaurantOutboxJpaRepository.deleteByTypeAndOutboxStatus(type, outboxStatus);
    }
}
