package com.berkay.restaurant.service.domain.ports.input.message.listener;

import com.berkay.restaurant.service.domain.dto.message.RestaurantRatingUpdateModel;

public interface RestaurantRatingMessageListener {
    void updateRating(RestaurantRatingUpdateModel model);
}
