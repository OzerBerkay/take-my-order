package com.berkay.restaurant.service.domain.event;

import com.berkay.domain.event.DomainEvent;
import com.berkay.restaurant.service.domain.entity.Restaurant;

import java.time.ZonedDateTime;

public class RestaurantCreatedEvent implements DomainEvent<Restaurant> {

    private final Restaurant restaurant;
    private final ZonedDateTime createdAt;

    public RestaurantCreatedEvent(Restaurant restaurant, ZonedDateTime createdAt) {
        this.restaurant = restaurant;
        this.createdAt = createdAt;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

}
