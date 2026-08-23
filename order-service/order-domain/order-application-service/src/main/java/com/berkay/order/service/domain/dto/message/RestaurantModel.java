package com.berkay.order.service.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantModel {
    private UUID restaurantId;
    private String name;
    private boolean active;
    private boolean available;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryFee;

    private List<ProductModel> products;
}
