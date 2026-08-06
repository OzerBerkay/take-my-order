package com.berkay.identity.service.handler.address;

import com.berkay.identity.service.domain.entity.Address;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.AddressId;
import com.berkay.identity.service.dto.query.UserAddressResponse;
import com.berkay.identity.service.ports.output.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryMyAddressByIdHandler {

    private final AddressRepository addressRepository;

    public UserAddressResponse getMyAddressById(UUID userId, UUID addressId) {
        log.info("Querying address {} for user: {}", addressId, userId);
        
        Address address = addressRepository.findById(new AddressId(addressId))
                .orElseThrow(() -> new IdentityDomainException("Address not found or does not belong to user"));

        if (!address.getUserId().getValue().equals(userId)) {
            throw new IdentityDomainException("Address not found or does not belong to user");
        }

        return UserAddressResponse.builder()
                .id(address.getId().getValue())
                .userId(address.getUserId().getValue())
                .name(address.getName())
                .street(address.getStreet())
                .postalCode(address.getPostalCode())
                .city(address.getCity())
                .country(address.getCountry())
                .build();
    }
}
