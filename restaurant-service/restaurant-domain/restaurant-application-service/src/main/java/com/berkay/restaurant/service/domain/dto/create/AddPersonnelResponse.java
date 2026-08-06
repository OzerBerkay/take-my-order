package com.berkay.restaurant.service.domain.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AddPersonnelResponse {
    private final UUID personnelId;
    private final UUID restaurantId;
    private final String message;
}
