package com.berkay.restaurant.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.OrderApprovalStatus;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.valueobject.OrderApprovalId;

public class OrderApproval extends BaseEntity<OrderApprovalId> {
    private final RestaurantId restaurantId;
    private final OrderId orderId;
    private OrderApprovalStatus approvalStatus;
    private java.util.Map<com.berkay.domain.valueobject.ProductId, Integer> productQuantities;

    private OrderApproval(Builder builder) {
        setId(builder.orderApprovalId);
        restaurantId = builder.restaurantId;
        orderId = builder.orderId;
        approvalStatus = builder.approvalStatus;
        productQuantities = builder.productQuantities;
    }

    public static Builder builder() {
        return new Builder();
    }


    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    
    public void setApprovalStatus(OrderApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public OrderApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public java.util.Map<com.berkay.domain.valueobject.ProductId, Integer> getProductQuantities() {
        return productQuantities;
    }

    public static final class Builder {
        private OrderApprovalId orderApprovalId;
        private RestaurantId restaurantId;
        private OrderId orderId;
        private OrderApprovalStatus approvalStatus;
        private java.util.Map<com.berkay.domain.valueobject.ProductId, Integer> productQuantities;

        private Builder() {
        }

        public Builder orderApprovalId(OrderApprovalId val) {
            orderApprovalId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder approvalStatus(OrderApprovalStatus val) {
            approvalStatus = val;
            return this;
        }

        public Builder productQuantities(java.util.Map<com.berkay.domain.valueobject.ProductId, Integer> val) {
            productQuantities = val;
            return this;
        }

        public OrderApproval build() {
            return new OrderApproval(this);
        }
    }
}
