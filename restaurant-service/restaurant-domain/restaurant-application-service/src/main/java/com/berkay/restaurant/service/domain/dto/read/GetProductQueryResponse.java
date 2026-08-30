package com.berkay.restaurant.service.domain.dto.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetProductQueryResponse {
    private final UUID productId;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Integer stock;
    private final Boolean available;
    private final Boolean hidden;
    private final String imageUrl;
    private final UUID categoryId;
}
