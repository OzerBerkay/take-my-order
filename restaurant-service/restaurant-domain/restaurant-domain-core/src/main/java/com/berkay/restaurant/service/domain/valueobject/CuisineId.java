package com.berkay.restaurant.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;
import java.util.UUID;

public class CuisineId extends BaseId<UUID> {
    public CuisineId(UUID value) {
        super(value);
    }
}
