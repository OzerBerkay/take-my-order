package com.berkay.restaurant.service.domain.valueobject;

import java.util.Objects;

public class RestaurantName {
    private final String restaurantName;

    public RestaurantName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be empty!");
        }
        this.restaurantName = value;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantName that = (RestaurantName) o;
        return restaurantName.equals(that.restaurantName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantName);
    }
}
