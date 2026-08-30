package com.berkay.restaurant.service.domain.dto.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoriesRequest {
    @NotNull(message = "Category version cannot be null!")
    private Long categoryVersion;

    @Valid
    @NotNull(message = "Categories list cannot be null!")
    private List<CategoryItemRequest> categories;
}
