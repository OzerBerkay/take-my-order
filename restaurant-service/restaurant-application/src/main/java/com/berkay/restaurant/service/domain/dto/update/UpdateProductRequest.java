package com.berkay.restaurant.service.domain.dto.update;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {

    @NotNull(message = "Product name cannot be null!")
    @Size(min = 2, max = 50, message = "Product name must be between 2 and 50 characters!")
    private String name;

    @NotNull(message = "Price cannot be null!")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0!")
    private BigDecimal price;

    @NotNull(message = "Stock cannot be null!")
    @Min(value = 0, message = "Stock cannot be lower than 0!")
    private Integer stock;

    @NotNull(message = "Availability status cannot be null!")
    private Boolean available;
}
