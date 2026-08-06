package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.entity.Address;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.AddressId;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.AddAddressCommand;
import com.berkay.identity.service.dto.command.AddressResponse;
import com.berkay.identity.service.dto.command.UpdateAddressCommand;
import com.berkay.identity.service.ports.output.repository.AddressRepository;
import com.berkay.identity.service.ports.output.security.SecurityContextPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressCommandHandler {

    private final AddressRepository addressRepository;
    private final SecurityContextPort securityContextPort;

    @Transactional
    public AddressResponse addAddress(AddAddressCommand command) {
        UUID currentUserId = securityContextPort.getCurrentInternalUserId();
        log.info("Adding address for user id: {}", currentUserId);

        Address address = Address.create(
                new UserId(currentUserId),
                command.getName(),
                command.getStreet(),
                command.getCity(),
                command.getPostalCode(),
                command.getCountry()
        );

        address = addressRepository.save(address);

        return AddressResponse.builder()
                .addressId(address.getId().getValue())
                .message("Address created successfully")
                .build();
    }

    @Transactional
    public AddressResponse updateAddress(UpdateAddressCommand command) {
        UUID currentUserId = securityContextPort.getCurrentInternalUserId();
        log.info("Updating address id: {} for user id: {}", command.getAddressId(), currentUserId);

        Address address = addressRepository.findById(new AddressId(command.getAddressId()))
                .orElseThrow(() -> new IdentityDomainException("Address not found!"));

        if (!address.getUserId().getValue().equals(currentUserId)) {
            log.error("IDOR Attempt! User {} tried to update address {} belonging to user {}", currentUserId, command.getAddressId(), address.getUserId().getValue());
            throw new IdentityDomainException("You are not authorized to update this address!");
        }

        Address updatedAddress = Address.builder()
                .addressId(address.getId())
                .userId(address.getUserId())
                .name(command.getName())
                .street(command.getStreet())
                .city(command.getCity())
                .postalCode(command.getPostalCode())
                .country(command.getCountry())
                .build();

        updatedAddress = addressRepository.save(updatedAddress);

        return AddressResponse.builder()
                .addressId(updatedAddress.getId().getValue())
                .message("Address updated successfully")
                .build();
    }
}
