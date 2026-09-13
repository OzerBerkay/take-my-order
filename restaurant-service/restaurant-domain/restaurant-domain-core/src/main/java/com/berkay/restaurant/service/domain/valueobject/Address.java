package com.berkay.restaurant.service.domain.valueobject;

import java.util.Objects;

public class Address {
    private final String city;
    private final String district;
    private final String neighborhood;
    private final String street;
    private final String buildingNumber;
    private final String doorNumber;

    public Address(String city, String district, String neighborhood, String street, String buildingNumber, String doorNumber) {
        this.city = city;
        this.district = district;
        this.neighborhood = neighborhood;
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.doorNumber = doorNumber;
    }

    public String getCity() { return city; }
    public String getDistrict() { return district; }
    public String getNeighborhood() { return neighborhood; }
    public String getStreet() { return street; }
    public String getBuildingNumber() { return buildingNumber; }
    public String getDoorNumber() { return doorNumber; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(city, address.city) &&
                Objects.equals(district, address.district) &&
                Objects.equals(neighborhood, address.neighborhood) &&
                Objects.equals(street, address.street) &&
                Objects.equals(buildingNumber, address.buildingNumber) &&
                Objects.equals(doorNumber, address.doorNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, district, neighborhood, street, buildingNumber, doorNumber);
    }
}
