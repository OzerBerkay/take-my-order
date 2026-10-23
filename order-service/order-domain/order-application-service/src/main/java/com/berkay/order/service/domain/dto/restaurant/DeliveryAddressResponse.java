package com.berkay.order.service.domain.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddressResponse {
    private String city;
    private String district;
    private String neighborhood;
    private String street;
    private String buildingNumber;
    private String doorNumber;
    private Integer floor;
    private String addressInstructions;
    private String contactFirstName;
    private String contactLastName;
    private String contactPhone;
}
