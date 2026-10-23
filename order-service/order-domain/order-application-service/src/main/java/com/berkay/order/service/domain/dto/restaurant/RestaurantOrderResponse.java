package com.berkay.order.service.domain.dto.restaurant;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.MaterialAction;
import com.berkay.domain.valueobject.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantOrderResponse {
    private UUID orderId;
    private UUID trackingId;
    private UUID restaurantId;
    private OrderStatus orderStatus;
    private BigDecimal price;
    private BigDecimal deliveryFee;
    private ZonedDateTime createdAt;
    private List<RestaurantOrderItemResponse> items;
    private DeliveryAddressResponse deliveryAddress;
    private CancelReason cancelReason;
    private MaterialAction materialAction;
    private String internalNote;
}
