package com.berkay.order.service.domain.ports.input.message.listener.restaurant;

import com.berkay.order.service.domain.dto.message.RestaurantModel;

public interface RestaurantCreatedMessageListener {

    // Restoran oluşturulduğunda tetiklenir
    void restaurantCreated(RestaurantModel restaurantModel);
}
