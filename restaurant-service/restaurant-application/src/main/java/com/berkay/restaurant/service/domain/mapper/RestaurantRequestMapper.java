package com.berkay.restaurant.service.domain.mapper;

import com.berkay.restaurant.service.domain.dto.update.UpdateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.update.UpdateRestaurantRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RestaurantRequestMapper {

    public UpdateRestaurantCommand updateRestaurantRequestToCommand(UUID restaurantId, UpdateRestaurantRequest request) {
        return UpdateRestaurantCommand.builder()
                .restaurantId(restaurantId)
                .restaurantName(request.getRestaurantName())
                .active(request.getActive())
                .build();
    }
}
