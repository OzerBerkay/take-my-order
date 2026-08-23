package com.berkay.order.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.valueobject.OrderItemId;

public class OrderItem extends BaseEntity<OrderItemId> {
    private OrderId orderId;
    // DTO içinde sadece productId var. Entity'de obje olmasının sebebi restoranın orijinal product'ını karşılaştırmalarda kullanmak.
    private final Product product;
    private final int quantity;
    private final Money price;
    private final Money subTotal;

    private OrderItem(Builder builder) {
        super.setId(builder.orderItemId);
        product = builder.product;
        quantity = builder.quantity;
        price = builder.price;
        subTotal = builder.subTotal;
    }

    void initializeOrderItem(OrderId orderId, OrderItemId orderItemId) {
        this.orderId = orderId;
        super.setId(orderItemId);
    }

    void validatePrice() {
        if (quantity <= 0) {
            throw new OrderDomainException("Order Item quantity must be greater than zero for product " + product.getId().getValue());
        }
        
        // 1. Kontrol: Fiyat 0'dan büyük olmalı
        if (!price.isGreaterThanZero()) {
            throw new OrderDomainException("Order Item Price: " + price.getAmount() + " is not valid for product " + product.getId().getValue() + ". Price must be greater than zero!");
        }

        // 2. Kontrol: Ürün fiyatı ile sipariş fiyatı uyuşuyor mu?
        if (!price.equals(product.getPrice())) {
            throw new OrderDomainException("Order Item Price: " + price.getAmount() + " is not valid for product " + product.getId().getValue() +
                    ". Price mismatch! Order Item Price: " + price.getAmount() +
                    ", Product Actual Price: " + product.getPrice().getAmount());
        }

        // 3. Kontrol: Birim Fiyat * Adet = Ara Toplam mı?
        if (!price.multiply(quantity).equals(subTotal)) {
            throw new OrderDomainException("Order Item Price: " + price.getAmount() + " is not valid for product " + product.getId().getValue() +
                    ". SubTotal is not correct! " +
                    "Price: " + price.getAmount() + " * Quantity: " + quantity +
                    " != SubTotal: " + subTotal.getAmount());
        }
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getPrice() {
        return price;
    }

    public Money getSubTotal() {
        return subTotal;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Product product;
        private int quantity;
        private Money price;
        private Money subTotal;
        private OrderItemId orderItemId;

        private Builder() {
        }

        public Builder product(Product val) {
            product = val;
            return this;
        }

        public Builder quantity(int val) {
            quantity = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder subTotal(Money val) {
            subTotal = val;
            return this;
        }

        public Builder orderItemId(OrderItemId val) {
            orderItemId = val;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);
        }
    }
}
