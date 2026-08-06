package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.BaseId;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitId;
import com.berkay.identity.service.domain.valueobject.OrganizationalUnitType;

public class OrganizationalUnit extends AggregateRoot<OrganizationalUnitId> {

    private final String name;
    private final OrganizationalUnitType type;

    private OrganizationalUnit(Builder builder) {
        super.setId(builder.id);
        this.name = builder.name;
        this.type = builder.type;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public OrganizationalUnitType getType() {
        return type;
    }

    public static final class Builder {
        private OrganizationalUnitId id;
        private String name;
        private OrganizationalUnitType type;

        private Builder() {
        }

        public Builder id(OrganizationalUnitId val) {
            id = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder type(OrganizationalUnitType val) {
            type = val;
            return this;
        }

        public OrganizationalUnit build() {
            return new OrganizationalUnit(this);
        }
    }
}
