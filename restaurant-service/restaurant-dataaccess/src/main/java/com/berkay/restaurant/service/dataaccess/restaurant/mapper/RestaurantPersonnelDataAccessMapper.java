package com.berkay.restaurant.service.dataaccess.restaurant.mapper;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantPersonnelEntity;
import com.berkay.restaurant.service.domain.entity.RestaurantPersonnel;
import com.berkay.restaurant.service.domain.valueobject.RestaurantPersonnelId;
import org.springframework.stereotype.Component;

@Component
public class RestaurantPersonnelDataAccessMapper {

    public RestaurantPersonnelEntity restaurantPersonnelToRestaurantPersonnelEntity(RestaurantPersonnel personnel) {
        return RestaurantPersonnelEntity.builder()
                .id(personnel.getId().getValue())
                .restaurantId(personnel.getRestaurantId().getValue())
                .userId(personnel.getUserId())
                .createdAt(personnel.getCreatedAt())
                .build();
    }

    public RestaurantPersonnel restaurantPersonnelEntityToRestaurantPersonnel(RestaurantPersonnelEntity entity) {
        return RestaurantPersonnel.builder()
                .restaurantPersonnelId(new RestaurantPersonnelId(entity.getId()))
                .restaurantId(new RestaurantId(entity.getRestaurantId()))
                .userId(entity.getUserId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
