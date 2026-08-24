package com.berkay.restaurant.service.domain.dto.update.cuisine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateCuisineCommand {

    // Can be partially updated, but for this implementation we assume full patch for fields except if null
    private final String name;
    
    private final String code;
    
    private final String description;
    
    private final String iconUrl;
    
    @JsonProperty("active")
    private final Boolean isActive;
}
