package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.OrderApprovalEvent;
import com.berkay.restaurant.service.domain.event.RestaurantInformationEvent;

import java.util.List;

public interface RestaurantDomainService {

    OrderApprovalEvent validateOrder(Restaurant restaurant, List<String> failureMessages);

    RestaurantInformationEvent validateAndInitiateRestaurant(Restaurant restaurant);
}
