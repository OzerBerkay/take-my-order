package com.berkay.restaurant.service.domain.dto.delete;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RemovePersonnelResponse {
    private final UUID restaurantId;
    private final UUID userId;
    private final String message;
}
