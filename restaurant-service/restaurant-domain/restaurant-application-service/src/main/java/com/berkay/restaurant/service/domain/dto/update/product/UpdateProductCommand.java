package com.berkay.restaurant.service.domain.dto.update.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UpdateProductCommand { // Fiyat veya stok durumu güncellemek için
    private final UUID restaurantId;
    private final UUID productId;
    private final String name;
    private final BigDecimal price;
    private final boolean available;
}
