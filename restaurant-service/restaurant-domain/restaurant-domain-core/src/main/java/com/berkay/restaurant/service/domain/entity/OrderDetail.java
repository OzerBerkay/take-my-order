package com.berkay.restaurant.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.domain.valueobject.ProductId;

import java.util.Map;

public class OrderDetail extends BaseEntity<OrderId> {
    private OrderStatus orderStatus;
    private Money totalAmount;
    private final Map<ProductId, Integer> productQuantities;

    private OrderDetail(Builder builder) {
        setId(builder.orderId);
        orderStatus = builder.orderStatus;
        totalAmount = builder.totalAmount;
        productQuantities = builder.productQuantities;
    }

    public static Builder builder() {
        return new Builder();
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public Map<ProductId, Integer> getProductQuantities() {
        return productQuantities;
    }

    public static final class Builder {
        private OrderId orderId;
        private OrderStatus orderStatus;
        private Money totalAmount;
        private Map<ProductId, Integer> productQuantities;

        private Builder() {
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder totalAmount(Money val) {
            totalAmount = val;
            return this;
        }

        public Builder productQuantities(Map<ProductId, Integer> val) {
            productQuantities = val;
            return this;
        }

        public OrderDetail build() {
            return new OrderDetail(this);
        }
    }
}
