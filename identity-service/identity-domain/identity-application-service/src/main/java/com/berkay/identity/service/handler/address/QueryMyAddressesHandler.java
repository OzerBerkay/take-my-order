package com.berkay.identity.service.handler.address;

import com.berkay.identity.service.domain.entity.Address;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.dto.query.UserAddressResponse;
import com.berkay.identity.service.ports.output.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryMyAddressesHandler {

    private final AddressRepository addressRepository;

    public List<UserAddressResponse> getMyAddresses(UUID userId) {
        log.info("Querying addresses for user: {}", userId);
        
        List<Address> addresses = addressRepository.findByUserId(new com.berkay.identity.service.domain.valueobject.UserId(userId));

        return addresses.stream().map(address -> UserAddressResponse.builder()
                .id(address.getId().getValue())
                .userId(address.getUserId().getValue())
                .name(address.getName())
                .street(address.getStreet())
                .postalCode(address.getPostalCode())
                .city(address.getCity())
                .country(address.getCountry())
                .build()).collect(Collectors.toList());
    }
}
