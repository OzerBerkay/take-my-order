package com.berkay.order.service.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public class StreetAddress {
    private final UUID id;
    private final String city;
    private final String district;
    private final String neighborhood;
    private final String street;
    private final String buildingNumber;
    private final String doorNumber;
    private final Integer floor;
    private final String addressInstructions;
    private final String contactFirstName;
    private final String contactLastName;
    private final String contactPhone;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StreetAddress that = (StreetAddress) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(city, that.city) && 
               Objects.equals(district, that.district) && 
               Objects.equals(neighborhood, that.neighborhood) && 
               Objects.equals(street, that.street) && 
               Objects.equals(buildingNumber, that.buildingNumber) && 
               Objects.equals(doorNumber, that.doorNumber) && 
               Objects.equals(floor, that.floor) && 
               Objects.equals(addressInstructions, that.addressInstructions) && 
               Objects.equals(contactFirstName, that.contactFirstName) && 
               Objects.equals(contactLastName, that.contactLastName) && 
               Objects.equals(contactPhone, that.contactPhone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, city, district, neighborhood, street, buildingNumber, doorNumber, floor, addressInstructions, contactFirstName, contactLastName, contactPhone);
    }

    public UUID getId() { return id; }
    public String getCity() { return city; }
    public String getDistrict() { return district; }
    public String getNeighborhood() { return neighborhood; }
    public String getStreet() { return street; }
    public String getBuildingNumber() { return buildingNumber; }
    public String getDoorNumber() { return doorNumber; }
    public Integer getFloor() { return floor; }
    public String getAddressInstructions() { return addressInstructions; }
    public String getContactFirstName() { return contactFirstName; }
    public String getContactLastName() { return contactLastName; }
    public String getContactPhone() { return contactPhone; }

    public StreetAddress(UUID id, String city, String district, String neighborhood, String street, String buildingNumber, String doorNumber, Integer floor, String addressInstructions, String contactFirstName, String contactLastName, String contactPhone) {
        this.id = id;
        this.city = city;
        this.district = district;
        this.neighborhood = neighborhood;
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.doorNumber = doorNumber;
        this.floor = floor;
        this.addressInstructions = addressInstructions;
        this.contactFirstName = contactFirstName;
        this.contactLastName = contactLastName;
        this.contactPhone = contactPhone;
    }
}
