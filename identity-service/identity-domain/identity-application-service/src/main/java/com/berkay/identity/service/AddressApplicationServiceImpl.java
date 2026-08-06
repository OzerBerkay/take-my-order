package com.berkay.identity.service;

import com.berkay.identity.service.dto.command.AddAddressCommand;
import com.berkay.identity.service.dto.command.AddressResponse;
import com.berkay.identity.service.dto.command.UpdateAddressCommand;
import com.berkay.identity.service.handler.AddressCommandHandler;
import com.berkay.identity.service.handler.address.QueryMyAddressesHandler;
import com.berkay.identity.service.handler.address.QueryMyAddressByIdHandler;
import com.berkay.identity.service.handler.address.DeleteMyAddressCommandHandler;
import com.berkay.identity.service.ports.input.service.AddressApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class AddressApplicationServiceImpl implements AddressApplicationService {

    private final AddressCommandHandler addressCommandHandler;
    private final QueryMyAddressesHandler queryMyAddressesHandler;
    private final QueryMyAddressByIdHandler queryMyAddressByIdHandler;
    private final DeleteMyAddressCommandHandler deleteMyAddressCommandHandler;

    @Override
    public AddressResponse addAddress(AddAddressCommand command) {
        return addressCommandHandler.addAddress(command);
    }

    @Override
    public AddressResponse updateAddress(UpdateAddressCommand command) {
        return addressCommandHandler.updateAddress(command);
    }

    @Override
    public java.util.List<com.berkay.identity.service.dto.query.UserAddressResponse> getMyAddresses(java.util.UUID userId) {
        return queryMyAddressesHandler.getMyAddresses(userId);
    }

    @Override
    public com.berkay.identity.service.dto.query.UserAddressResponse getMyAddressById(java.util.UUID userId, java.util.UUID addressId) {
        return queryMyAddressByIdHandler.getMyAddressById(userId, addressId);
    }

    @Override
    public void deleteMyAddress(java.util.UUID userId, java.util.UUID addressId) {
        deleteMyAddressCommandHandler.deleteMyAddress(userId, addressId);
    }
}
