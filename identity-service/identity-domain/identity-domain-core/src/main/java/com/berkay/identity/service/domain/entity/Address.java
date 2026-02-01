package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.identity.service.domain.valueobject.AddressId;

import java.util.UUID;

public class Address extends BaseEntity<AddressId> {
    private final String name;
    private final String street;
    private final String postalCode;
    private final String city;
    private final String country;

    // Private Constructor: Sadece içeriden (create veya builder) erişilebilir
    private Address(Builder builder) {
        super.setId(builder.addressId);
        this.name = builder.name;
        this.street = builder.street;
        this.postalCode = builder.postalCode;
        this.city = builder.city;
        this.country = builder.country;
    }

    // FACTORY METHOD: ID üretimini ve nesne oluşturmayı garanti altına alır.
    public static Address create(String name, String street, String city, String postalCode, String country) {
        return new Builder()
                .addressId(new AddressId(UUID.randomUUID()))
                .name(name)
                .street(street)
                .city(city)
                .postalCode(postalCode)
                .country(country)
                .build();
    }

    public String getName() { return name; }
    public String getStreet() { return street; }
    public String getPostalCode() { return postalCode; }
    public String getCity() { return city; }
    public String getCountry() { return country; }

    public static final class Builder {
        private AddressId addressId;
        private String name;
        private String street;
        private String postalCode;
        private String city;
        private String country;

        private Builder() {}

        // create() metodu bunu kullanacak
        public Builder addressId(AddressId val) { addressId = val; return this; }
        public Builder name(String val) { name = val; return this; }
        public Builder street(String val) { street = val; return this; }
        public Builder postalCode(String val) { postalCode = val; return this; }
        public Builder city(String val) { city = val; return this; }
        public Builder country(String val) { country = val; return this; }

        public Address build() { return new Address(this); }
    }
}