package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.domain.entity.Address;
import com.berkay.identity.service.domain.valueobject.AddressId;

import java.util.Optional;

public interface AddressRepository {
    Address save(Address address);
    Optional<Address> findById(AddressId addressId);
    java.util.List<Address> findByUserId(com.berkay.identity.service.domain.valueobject.UserId userId);
    void deleteById(AddressId addressId);
}
