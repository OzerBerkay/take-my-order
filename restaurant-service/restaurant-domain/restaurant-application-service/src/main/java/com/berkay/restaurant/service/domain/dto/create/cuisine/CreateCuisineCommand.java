package com.berkay.restaurant.service.domain.dto.create.cuisine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateCuisineCommand {
    @NotBlank(message = "Cuisine name cannot be blank")
    private final String name;

    @NotBlank(message = "Cuisine code cannot be blank")
    private final String code;

    @NotBlank(message = "Cuisine description cannot be blank")
    private final String description;

    @NotBlank(message = "Cuisine icon url cannot be blank")
    private final String iconUrl;

    @NotNull(message = "active flag is required")
    @JsonProperty("active")
    private final Boolean isActive;
}
