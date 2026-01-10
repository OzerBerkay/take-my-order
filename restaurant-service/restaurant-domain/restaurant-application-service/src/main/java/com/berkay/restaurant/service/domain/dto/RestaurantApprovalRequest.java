package com.berkay.restaurant.service.domain.dto;

import com.berkay.domain.valueobject.RestaurantOrderStatus;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantApprovalRequest {
    private String id;
    private String sagaId;
    private String restaurantId;
    private String orderId;
    private RestaurantOrderStatus restaurantOrderStatus;
    private List<ProductQuantity> productQuantities;
    private java.math.BigDecimal price;
    private java.time.Instant createdAt;

    // Domain Entity'den bağımsız, sadece veri taşıyıcı inner class
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductQuantity {
        private String id;
        private int quantity;
    }
}
