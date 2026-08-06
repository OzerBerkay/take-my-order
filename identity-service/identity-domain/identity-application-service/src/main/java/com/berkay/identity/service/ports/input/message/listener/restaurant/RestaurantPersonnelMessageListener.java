package com.berkay.identity.service.ports.input.message.listener.restaurant;

import com.berkay.kafka.order.avro.model.RestaurantPersonnelAvroModel;

public interface RestaurantPersonnelMessageListener {
    void personnelAdded(RestaurantPersonnelAvroModel payload);
}
