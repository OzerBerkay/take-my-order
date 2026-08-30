package com.berkay.restaurant.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.ProductCategoryId;

public class ProductCategory extends BaseEntity<ProductCategoryId> {
    private String name;
    private int sortOrder;

    private ProductCategory(Builder builder) {
        setId(builder.productCategoryId);
        name = builder.name;
        sortOrder = builder.sortOrder;
    }

    public void update(String name, int sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public static final class Builder {
        private ProductCategoryId productCategoryId;
        private String name;
        private int sortOrder;

        private Builder() {
        }

        public Builder productCategoryId(ProductCategoryId val) {
            productCategoryId = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder sortOrder(int val) {
            sortOrder = val;
            return this;
        }

        public ProductCategory build() {
            return new ProductCategory(this);
        }
    }
}
