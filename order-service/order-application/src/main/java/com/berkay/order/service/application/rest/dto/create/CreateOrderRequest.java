package com.berkay.order.service.application.rest.dto.create;

import com.berkay.order.service.domain.dto.create.OrderAddress;
import com.berkay.order.service.domain.dto.create.OrderItem;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreateOrderRequest {
    @NotNull
    private final UUID restaurantId;
    @NotNull
    private final UUID addressId; // Veya full adres objesi
    @NotNull
    private final BigDecimal price;
    @NotNull
    private final List<OrderItem> items;
    @NotNull
    private final OrderAddress address;

    // DİKKAT: customerId YOK! Onu token'dan alacağız.
}