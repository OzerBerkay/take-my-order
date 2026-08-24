package com.berkay.restaurant.service.domain.mapper;

import com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateRestaurantCommand;
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
                .available(request.getAvailable())
                .minimumOrderAmount(request.getMinimumOrderAmount())
                .deliveryFee(request.getDeliveryFee())
                .street(request.getStreet())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .phoneNumber(request.getPhoneNumber())
                .averageDeliveryTimeInMinutes(request.getAverageDeliveryTimeInMinutes())
                .cuisineIds(request.getCuisineIds())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .bannerUrl(request.getBannerUrl())
                .build();
    }
}
