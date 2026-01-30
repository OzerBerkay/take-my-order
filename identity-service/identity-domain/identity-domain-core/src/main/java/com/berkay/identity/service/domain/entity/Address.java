package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.BaseId;
import java.util.UUID;

// AddressId'yi generic BaseId<UUID> olarak kullanıyoruz
public class Address extends BaseEntity<BaseId<UUID>> {
    private String name; // Örn: "Ev", "İş" (Bunu eklemeliyiz, profil yönetimi için şart)
    private String street;
    private String postalCode;
    private String city;
    private String country;

    private Address(Builder builder) {
        super.setId(builder.id);
        this.name = builder.name;
        this.street = builder.street;
        this.postalCode = builder.postalCode;
        this.city = builder.city;
        this.country = builder.country;
    }

    public static Builder builder() { return new Builder(); }

    public String getName() { return name; }
    public String getStreet() { return street; }
    public String getPostalCode() { return postalCode; }
    public String getCity() { return city; }
    public String getCountry() { return country; }

    public static final class Builder {
        private BaseId<UUID> id;
        private String name;
        private String street;
        private String postalCode;
        private String city;
        private String country;

        private Builder() {}

        public Builder id(BaseId<UUID> val) { id = val; return this; }
        public Builder name(String val) { name = val; return this; }
        public Builder street(String val) { street = val; return this; }
        public Builder postalCode(String val) { postalCode = val; return this; }
        public Builder city(String val) { city = val; return this; }
        public Builder country(String val) { country = val; return this; }

        public Address build() { return new Address(this); }
    }
}