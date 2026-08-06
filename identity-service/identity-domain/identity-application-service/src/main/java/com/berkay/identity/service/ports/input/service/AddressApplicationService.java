package com.berkay.identity.service.ports.input.service;

import com.berkay.identity.service.dto.command.AddAddressCommand;
import com.berkay.identity.service.dto.command.AddressResponse;
import com.berkay.identity.service.dto.command.UpdateAddressCommand;

public interface AddressApplicationService {
    AddressResponse addAddress(AddAddressCommand command);
    AddressResponse updateAddress(UpdateAddressCommand command);
    java.util.List<com.berkay.identity.service.dto.query.UserAddressResponse> getMyAddresses(java.util.UUID userId);
    com.berkay.identity.service.dto.query.UserAddressResponse getMyAddressById(java.util.UUID userId, java.util.UUID addressId);
    void deleteMyAddress(java.util.UUID userId, java.util.UUID addressId);
}
