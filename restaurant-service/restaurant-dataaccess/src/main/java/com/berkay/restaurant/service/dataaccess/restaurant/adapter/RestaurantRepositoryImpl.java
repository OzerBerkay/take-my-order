package com.berkay.restaurant.service.dataaccess.restaurant.adapter;

import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.berkay.restaurant.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.berkay.restaurant.service.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    public RestaurantRepositoryImpl(RestaurantJpaRepository restaurantJpaRepository,
                                    RestaurantDataAccessMapper restaurantDataAccessMapper) {
        this.restaurantJpaRepository = restaurantJpaRepository;
        this.restaurantDataAccessMapper = restaurantDataAccessMapper;
    }

    @Override
    public Optional<Restaurant> findRestaurantById(UUID restaurantId) {
        return restaurantJpaRepository.findById(restaurantId)
                .map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
    }

    @Override
    public Optional<Restaurant> findRestaurantByIdWithLock(UUID restaurantId) {
        return restaurantJpaRepository.findByIdWithLock(restaurantId)
                .map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
    }

    @Override
    public Restaurant saveRestaurant(Restaurant restaurant) {
        RestaurantEntity restaurantEntity = restaurantDataAccessMapper.restaurantToRestaurantEntity(restaurant);
        RestaurantEntity savedEntity = restaurantJpaRepository.save(restaurantEntity);
        return restaurantDataAccessMapper.restaurantEntityToRestaurant(savedEntity);
    }

    @Override
    public java.util.List<Restaurant> findAllByIdIn(java.util.List<UUID> restaurantIds) {
        return restaurantJpaRepository.findAllByRestaurantIdIn(restaurantIds).stream()
                .map(restaurantDataAccessMapper::restaurantEntityToRestaurant)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public com.berkay.restaurant.service.domain.dto.read.RestaurantPageResult findPublicRestaurants(String name, java.util.List<String> cuisineCodes, Boolean available, java.math.BigDecimal maxMinimumOrderAmount, Integer maxDeliveryTime, int page, int size) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<RestaurantEntity> pagedResult = restaurantJpaRepository.findPublicRestaurants(name, cuisineCodes, available, maxMinimumOrderAmount, maxDeliveryTime, pageRequest);
        
        java.util.List<Restaurant> restaurants = pagedResult.getContent().stream()
                .map(restaurantDataAccessMapper::restaurantEntityToRestaurant)
                .collect(java.util.stream.Collectors.toList());

        return new com.berkay.restaurant.service.domain.dto.read.RestaurantPageResult(
                restaurants,
                pagedResult.getNumber(),
                pagedResult.getSize(),
                pagedResult.getTotalElements(),
                pagedResult.getTotalPages(),
                pagedResult.isLast()
        );
    }
}