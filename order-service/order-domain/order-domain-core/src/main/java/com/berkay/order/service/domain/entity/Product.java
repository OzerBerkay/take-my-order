package com.berkay.order.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.ProductId;

public class Product extends BaseEntity<ProductId> {
    private String name;
    private Money price;
    private Boolean available;
    private boolean hidden;

    public Product(ProductId productId, String name, Money price, Boolean available, boolean hidden) {
        super.setId(productId);
        this.name = name;
        this.price = price;
        this.available = available;
        this.hidden = hidden;
    }

    public Product(ProductId productId) {
        super.setId(productId);
    }

    public void updateWithConfirmedNamePriceAndAvailability(String name, Money price, Boolean available, boolean hidden) {
        this.name = name;
        this.price = price;
        this.available = available;
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public boolean isAvailable() {return available;}
    public boolean isHidden() {return hidden;}
}
