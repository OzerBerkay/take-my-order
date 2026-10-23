package com.berkay.order.service.domain.dto.restaurant;

import com.berkay.domain.valueobject.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantOrdersQuery {
    @NotNull
    private UUID restaurantId;
    private OrderStatus status;
    private String search;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 20;
    private String sort;
}
