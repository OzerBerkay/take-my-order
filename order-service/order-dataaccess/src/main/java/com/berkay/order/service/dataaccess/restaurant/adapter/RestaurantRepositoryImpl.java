package com.berkay.order.service.dataaccess.restaurant.adapter;

import com.berkay.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.berkay.order.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.berkay.order.service.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    public RestaurantRepositoryImpl(RestaurantJpaRepository restaurantJpaRepository) {
        this.restaurantJpaRepository = restaurantJpaRepository;
        this.restaurantDataAccessMapper = new RestaurantDataAccessMapper();
    }

    @Override
    public Optional<Restaurant> findRestaurantWithProducts(UUID restaurantId, List<UUID> productIds) {
        return restaurantJpaRepository.findByRestaurantIdAndProductIds(restaurantId, productIds)
                .map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        RestaurantEntity restaurantEntity = restaurantDataAccessMapper.restaurantToRestaurantEntity(restaurant);
        RestaurantEntity savedEntity = restaurantJpaRepository.save(restaurantEntity);
        return restaurantDataAccessMapper.restaurantEntityToRestaurant(savedEntity);
    }
}
