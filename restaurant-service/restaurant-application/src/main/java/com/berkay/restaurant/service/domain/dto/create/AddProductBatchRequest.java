package com.berkay.restaurant.service.domain.dto.create;

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
public class AddProductBatchRequest {
    @Valid
    @NotNull(message = "Products list cannot be null!")
    private List<ProductItemRequest> products;
}
