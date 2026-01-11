package com.berkay.order.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.ProductId;

public class Product extends BaseEntity<ProductId> {
    private String name;
    private Money price;
    private Boolean available;

    public Product(ProductId productId, String name, Money price, Boolean available) {
        super.setId(productId);
        this.name = name;
        this.price = price;
        this.available = available;
    }

    public Product(ProductId productId) {
        super.setId(productId);
    }

    public void updateWithConfirmedNamePriceAndAvailability(String name, Money price, Boolean available) {
        this.name = name;
        this.price = price;
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public boolean isAvailable() {return available;}
}
