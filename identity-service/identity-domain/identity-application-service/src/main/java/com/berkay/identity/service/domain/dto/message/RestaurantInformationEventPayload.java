package com.berkay.identity.service.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;
import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantInformationEventPayload {
    private final UUID restaurantId;
    private final UUID merchantId;
    private final String name;
    private final boolean active;
    private final ZonedDateTime createdAt;
}
