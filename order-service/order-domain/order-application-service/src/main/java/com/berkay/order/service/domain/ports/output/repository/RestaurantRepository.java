package com.berkay.order.service.domain.ports.output.repository;

import com.berkay.order.service.domain.entity.Restaurant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository {

    Optional<Restaurant> findRestaurantWithProducts(UUID restaurantId, List<UUID> productIds);

    Optional<Restaurant> findRestaurantByRestaurantId(UUID restaurantId);

    Optional<Restaurant> findRestaurantByIdWithLock(UUID restaurantId);

    java.util.Map<UUID, String> findRestaurantNamesByIds(List<UUID> restaurantIds);

    Restaurant save(Restaurant restaurant);
}
