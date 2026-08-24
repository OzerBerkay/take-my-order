package com.berkay.restaurant.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.valueobject.CuisineId;
import java.util.UUID;
import java.util.regex.Pattern;

public class Cuisine extends AggregateRoot<CuisineId> {

    private String name;
    private String code;
    private String description;
    private String iconUrl;
    private boolean active;

    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("^[a-z0-9]+(?:_[a-z0-9]+)*$");

    public void update(String name, String code, String description, String iconUrl, boolean active) {
        validateCode(code);
        this.name = name;
        this.code = code;
        this.description = description;
        this.iconUrl = iconUrl;
        this.active = active;
    }

    public void initializeCuisine() {
        if (getId() == null) {
            setId(new CuisineId(UUID.randomUUID()));
        }
        validateCode(this.code);
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new RestaurantDomainException("Cuisine code cannot be null or empty!");
        }
        if (!SNAKE_CASE_PATTERN.matcher(code).matches()) {
            throw new RestaurantDomainException("Cuisine code must be lower-case and snake_case format! (e.g. fast_food)");
        }
    }

    private Cuisine(Builder builder) {
        setId(builder.cuisineId);
        name = builder.name;
        code = builder.code;
        description = builder.description;
        iconUrl = builder.iconUrl;
        active = builder.active;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public boolean isActive() {
        return active;
    }

    public static final class Builder {
        private CuisineId cuisineId;
        private String name;
        private String code;
        private String description;
        private String iconUrl;
        private boolean active;

        private Builder() {
        }

        public Builder cuisineId(CuisineId val) {
            cuisineId = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder code(String val) {
            code = val;
            return this;
        }

        public Builder description(String val) {
            description = val;
            return this;
        }

        public Builder iconUrl(String val) {
            iconUrl = val;
            return this;
        }

        public Builder active(boolean val) {
            active = val;
            return this;
        }

        public Cuisine build() {
            return new Cuisine(this);
        }
    }
}
