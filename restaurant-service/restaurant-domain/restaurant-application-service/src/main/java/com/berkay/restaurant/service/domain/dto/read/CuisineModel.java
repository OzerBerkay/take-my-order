package com.berkay.restaurant.service.domain.dto.read;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CuisineModel {
    private final UUID id;
    private final String name;
    private final String code;
    private final String description;
    private final String iconUrl;
    @JsonProperty("active")
    private final Boolean isActive;
}
