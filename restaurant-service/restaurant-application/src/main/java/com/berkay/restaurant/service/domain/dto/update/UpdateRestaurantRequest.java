package com.berkay.restaurant.service.domain.dto.update;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateRestaurantRequest {
    @Size(min = 2, max = 50, message = "Restaurant name must be between 2 and 50 characters")
    private String restaurantName;

    private Boolean active;

    private Boolean available;
}
