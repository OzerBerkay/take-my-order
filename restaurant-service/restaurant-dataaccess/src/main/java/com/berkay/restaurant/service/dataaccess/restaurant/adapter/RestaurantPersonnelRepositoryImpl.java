package com.berkay.restaurant.service.dataaccess.restaurant.adapter;

import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantPersonnelEntity;
import com.berkay.restaurant.service.dataaccess.restaurant.mapper.RestaurantPersonnelDataAccessMapper;
import com.berkay.restaurant.service.dataaccess.restaurant.repository.RestaurantPersonnelJpaRepository;
import com.berkay.restaurant.service.domain.entity.RestaurantPersonnel;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantPersonnelRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RestaurantPersonnelRepositoryImpl implements RestaurantPersonnelRepository {

    private final RestaurantPersonnelJpaRepository restaurantPersonnelJpaRepository;
    private final RestaurantPersonnelDataAccessMapper restaurantPersonnelDataAccessMapper;

    public RestaurantPersonnelRepositoryImpl(RestaurantPersonnelJpaRepository restaurantPersonnelJpaRepository,
                                             RestaurantPersonnelDataAccessMapper restaurantPersonnelDataAccessMapper) {
        this.restaurantPersonnelJpaRepository = restaurantPersonnelJpaRepository;
        this.restaurantPersonnelDataAccessMapper = restaurantPersonnelDataAccessMapper;
    }

    @Override
    public RestaurantPersonnel save(RestaurantPersonnel restaurantPersonnel) {
        RestaurantPersonnelEntity entity = restaurantPersonnelDataAccessMapper.restaurantPersonnelToRestaurantPersonnelEntity(restaurantPersonnel);
        RestaurantPersonnelEntity savedEntity = restaurantPersonnelJpaRepository.save(entity);
        return restaurantPersonnelDataAccessMapper.restaurantPersonnelEntityToRestaurantPersonnel(savedEntity);
    }

    @Override
    public boolean existsByRestaurantIdAndUserId(UUID restaurantId, UUID userId) {
        return restaurantPersonnelJpaRepository.existsByRestaurantIdAndUserId(restaurantId, userId);
    }

    @Override
    public void deleteByRestaurantIdAndUserId(UUID restaurantId, UUID userId) {
        restaurantPersonnelJpaRepository.deleteByRestaurantIdAndUserId(restaurantId, userId);
    }

    @Override
    public java.util.List<RestaurantPersonnel> findByRestaurantId(UUID restaurantId) {
        return restaurantPersonnelJpaRepository.findByRestaurantId(restaurantId).stream()
                .map(restaurantPersonnelDataAccessMapper::restaurantPersonnelEntityToRestaurantPersonnel)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<RestaurantPersonnel> findByUserId(UUID userId) {
        return restaurantPersonnelJpaRepository.findByUserId(userId).stream()
                .map(restaurantPersonnelDataAccessMapper::restaurantPersonnelEntityToRestaurantPersonnel)
                .collect(java.util.stream.Collectors.toList());
    }
}
