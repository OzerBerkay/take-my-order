package com.berkay.order.service.domain.ports.input.message.listener.restaurant;

import com.berkay.order.service.domain.dto.message.RestaurantModel;

public interface RestaurantInformationMessageListener {

    // Restoran oluşturulduğunda tetiklenir
    void restaurantInformationReceived(RestaurantModel restaurantModel);
}
