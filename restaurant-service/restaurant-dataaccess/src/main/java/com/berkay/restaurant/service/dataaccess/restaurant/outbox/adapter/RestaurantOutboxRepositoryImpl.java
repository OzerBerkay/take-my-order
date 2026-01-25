package com.berkay.restaurant.service.dataaccess.restaurant.outbox.adapter;

import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.dataaccess.restaurant.outbox.mapper.RestaurantOutboxDataAccessMapper;
import com.berkay.restaurant.service.dataaccess.restaurant.outbox.repository.RestaurantOutboxJpaRepository;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantOutboxRepository;
import org.springframework.stereotype.Component;

import java.util.List;
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
        return restaurantOutboxDataAccessMapper.restaurantOutboxEntityToRestaurantOutboxMessage(
                restaurantOutboxJpaRepository.save(
                        restaurantOutboxDataAccessMapper.restaurantOutboxMessageToRestaurantOutboxEntity(restaurantOutboxMessage)));
    }

    @Override
    public List<RestaurantOutboxMessage> findByTypeAndOutboxStatus(List<String> types, OutboxStatus outboxStatus) {
        return restaurantOutboxJpaRepository.findByTypeInAndOutboxStatus(types, outboxStatus)
                .stream()
                .map(restaurantOutboxDataAccessMapper::restaurantOutboxEntityToRestaurantOutboxMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByTypeAndOutboxStatus(List<String> types, OutboxStatus outboxStatus) {
        restaurantOutboxJpaRepository.deleteByTypeInAndOutboxStatus(types, outboxStatus);
    }
}
