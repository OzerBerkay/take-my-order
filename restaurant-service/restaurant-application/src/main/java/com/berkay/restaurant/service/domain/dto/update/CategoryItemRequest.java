package com.berkay.restaurant.service.domain.dto.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryItemRequest {
    private UUID id;

    @NotNull(message = "Category name cannot be null!")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters!")
    private String name;

    @NotNull(message = "Sort order cannot be null!")
    private Integer sortOrder;
}
