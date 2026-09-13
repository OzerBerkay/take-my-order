package com.berkay.order.service.domain.dto.create;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderAddress {
    @NotBlank(message = "City cannot be blank")
    @Size(max = 50, message = "City cannot exceed 50 characters")
    private final String city;
    
    @NotBlank(message = "District cannot be blank")
    @Size(max = 50, message = "District cannot exceed 50 characters")
    private final String district;
    
    @NotBlank(message = "Neighborhood cannot be blank")
    @Size(max = 50, message = "Neighborhood cannot exceed 50 characters")
    private final String neighborhood;
    
    @NotBlank(message = "Street cannot be blank")
    @Size(max = 50, message = "Street cannot exceed 50 characters")
    private final String street;
    
    @NotBlank(message = "Building number cannot be blank")
    @Size(max = 10, message = "Building number cannot exceed 10 characters")
    private final String buildingNumber;
    
    @NotBlank(message = "Door number cannot be blank")
    @Size(max = 10, message = "Door number cannot exceed 10 characters")
    private final String doorNumber;
    
    private final Integer floor;
    
    @Size(max = 250, message = "Address instructions cannot exceed 250 characters")
    private final String addressInstructions;
    
    @NotBlank(message = "Contact first name cannot be blank")
    @Size(max = 50, message = "Contact first name cannot exceed 50 characters")
    private final String contactFirstName;
    
    @NotBlank(message = "Contact last name cannot be blank")
    @Size(max = 50, message = "Contact last name cannot exceed 50 characters")
    private final String contactLastName;
    
    @NotBlank(message = "Contact phone cannot be blank")
    @Size(max = 20, message = "Contact phone cannot exceed 20 characters")
    private final String contactPhone;
}
