package com.berkay.restaurant.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

import java.util.UUID;

public class RestaurantPersonnelId extends BaseId<UUID> {
    public RestaurantPersonnelId(UUID value) {
        super(value);
    }
}
