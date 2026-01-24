package com.berkay.restaurant.service.domain.dto.base;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder // Miras alınan sınıflarda Builder kullanmak için şart
@NoArgsConstructor(force = true)
@AllArgsConstructor
public abstract class BaseProductCommand {
    @NotNull(message = "Product name cannot be null!")
    @Size(min = 2, max = 50, message = "Product name must be between 2 and 50 characters!")
    private final String name;
    @NotNull(message = "Price cannot be null!")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0!")
    private final BigDecimal price;
    @NotNull(message = "Stock cannot be null!")
    private final int stock;
    @NotNull(message = "Availability status cannot be null!")
    private final boolean available;
}
