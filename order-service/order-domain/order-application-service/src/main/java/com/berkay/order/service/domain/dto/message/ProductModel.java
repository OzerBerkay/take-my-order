package com.berkay.order.service.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ProductModel {
    private UUID productId;
    private String name;
    private BigDecimal price;
    private boolean available;
    private boolean hidden;
}
