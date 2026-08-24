package com.berkay.restaurant.service.dataaccess.restaurant.mapper;

import com.berkay.restaurant.service.dataaccess.restaurant.entity.CuisineEntity;
import com.berkay.restaurant.service.domain.entity.Cuisine;
import com.berkay.restaurant.service.domain.valueobject.CuisineId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CuisineDataAccessMapper {

    public Cuisine cuisineEntityToCuisine(CuisineEntity cuisineEntity) {
        return Cuisine.builder()
                .cuisineId(new CuisineId(cuisineEntity.getId()))
                .name(cuisineEntity.getName())
                .code(cuisineEntity.getCode())
                .description(cuisineEntity.getDescription())
                .iconUrl(cuisineEntity.getIconUrl())
                .active(cuisineEntity.getIsActive())
                .build();
    }

    public CuisineEntity cuisineToCuisineEntity(Cuisine cuisine) {
        return CuisineEntity.builder()
                .id(cuisine.getId().getValue())
                .name(cuisine.getName())
                .code(cuisine.getCode())
                .description(cuisine.getDescription())
                .iconUrl(cuisine.getIconUrl())
                .isActive(cuisine.isActive())
                .build();
    }
}
