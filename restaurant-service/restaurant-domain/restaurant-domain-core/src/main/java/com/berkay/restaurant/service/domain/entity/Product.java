package com.berkay.restaurant.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.ProductId;

public class Product extends BaseEntity<ProductId> {
    private String name;
    private Money price;
    private int stock;
    private boolean available;

    private Product(Builder builder) {
        setId(builder.productId);
        name = builder.name;
        price = builder.price;
        stock = builder.stock;
        available = builder.available;
    }

    public void updateWith(String name, Money price, boolean available) {
        this.name = name;
        this.price = price;
        this.available = available;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public boolean isAvailable() {
        return available;
    }

    public static final class Builder {
        private ProductId productId;
        private String name;
        private Money price;
        private int stock;
        private boolean available;

        private Builder() {
        }

        public Builder productId(ProductId val) {
            productId = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder stock(int val) {
            stock = val;
            return this;
        }

        public Builder available(boolean val) {
            available = val;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}