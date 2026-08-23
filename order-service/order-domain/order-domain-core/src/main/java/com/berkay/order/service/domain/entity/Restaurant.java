package com.berkay.order.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.RestaurantId;

import java.util.List;

public class Restaurant extends AggregateRoot<RestaurantId> {
    private List<Product> products;
    private boolean active;
    private boolean available;
    private String name;
    private Money minimumOrderAmount;
    private Money deliveryFee;

    private Restaurant(Builder builder) {
        super.setId(builder.restaurantId);
        products = builder.products;
        active = builder.active;
        available = builder.available;
        name = builder.name;
        minimumOrderAmount = builder.minimumOrderAmount;
        deliveryFee = builder.deliveryFee;
    }

    public void update(String name, boolean active, boolean available, List<Product> products, Money minimumOrderAmount, Money deliveryFee) {
        this.name = name;
        this.active = active;
        this.available = available;
        this.products = products; // Snapshot mantığı: Listeyi komple yenisiyle değiştiriyoruz.
        this.minimumOrderAmount = minimumOrderAmount;
        this.deliveryFee = deliveryFee;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Product> getProducts() {
        return products;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getName() {
        return name;
    }

    public Money getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public Money getDeliveryFee() {
        return deliveryFee;
    }

    public static final class Builder {
        private RestaurantId restaurantId;
        private List<Product> products;
        private boolean active;
        private boolean available;
        private String name;
        private Money minimumOrderAmount;
        private Money deliveryFee;

        private Builder() {
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder products(List<Product> val) {
            products = val;
            return this;
        }

        public Builder active(boolean val) {
            active = val;
            return this;
        }

        public Builder available(boolean val) {
            available = val;
            return this;
        }
        
        public Builder minimumOrderAmount(Money val) {
            minimumOrderAmount = val;
            return this;
        }

        public Builder deliveryFee(Money val) {
            deliveryFee = val;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this);
        }
    }
}
