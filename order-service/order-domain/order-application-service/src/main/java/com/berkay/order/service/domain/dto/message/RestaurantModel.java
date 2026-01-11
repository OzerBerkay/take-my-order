package com.berkay.order.service.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantModel {
    private UUID restaurantId;
    private String name;
    private boolean active;
    private List<ProductModel> products;
}
