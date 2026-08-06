package com.berkay.identity.service.dataaccess.address.mapper;

import com.berkay.identity.service.dataaccess.address.entity.AddressEntity;
import com.berkay.identity.service.domain.entity.Address;
import com.berkay.identity.service.domain.valueobject.AddressId;
import com.berkay.identity.service.domain.valueobject.UserId;
import org.springframework.stereotype.Component;

@Component
public class AddressDataAccessMapper {

    public AddressEntity addressToAddressEntity(Address address) {
        return AddressEntity.builder()
                .id(address.getId().getValue())
                .userId(address.getUserId().getValue())
                .name(address.getName())
                .street(address.getStreet())
                .postalCode(address.getPostalCode())
                .city(address.getCity())
                .country(address.getCountry())
                .build();
    }

    public Address addressEntityToAddress(AddressEntity entity) {
        return Address.builder()
                .addressId(new AddressId(entity.getId()))
                .userId(new UserId(entity.getUserId()))
                .name(entity.getName())
                .street(entity.getStreet())
                .postalCode(entity.getPostalCode())
                .city(entity.getCity())
                .country(entity.getCountry())
                .build();
    }
}
