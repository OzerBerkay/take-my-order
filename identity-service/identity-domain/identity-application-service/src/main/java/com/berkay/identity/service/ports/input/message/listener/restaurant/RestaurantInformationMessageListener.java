package com.berkay.identity.service.ports.input.message.listener.restaurant;

import com.berkay.identity.service.domain.dto.message.RestaurantInformationEventPayload;

public interface RestaurantInformationMessageListener {
    void restaurantCreated(RestaurantInformationEventPayload payload);
}
