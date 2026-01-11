package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.OrderApprovalEvent;
import com.berkay.restaurant.service.domain.event.RestaurantCreatedEvent;

import java.util.List;

public interface RestaurantDomainService {

    OrderApprovalEvent validateOrder(Restaurant restaurant, List<String> failureMessages);

    RestaurantCreatedEvent validateAndInitiateRestaurant(Restaurant restaurant);
}
