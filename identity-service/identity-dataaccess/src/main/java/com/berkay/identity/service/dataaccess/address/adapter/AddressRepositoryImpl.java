package com.berkay.identity.service.dataaccess.address.adapter;

import com.berkay.identity.service.dataaccess.address.mapper.AddressDataAccessMapper;
import com.berkay.identity.service.dataaccess.address.repository.AddressJpaRepository;
import com.berkay.identity.service.domain.entity.Address;
import com.berkay.identity.service.domain.valueobject.AddressId;
import com.berkay.identity.service.ports.output.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {

    private final AddressJpaRepository addressJpaRepository;
    private final AddressDataAccessMapper addressDataAccessMapper;

    @Override
    public Address save(Address address) {
        return addressDataAccessMapper.addressEntityToAddress(
                addressJpaRepository.save(
                        addressDataAccessMapper.addressToAddressEntity(address)
                )
        );
    }

    @Override
    public Optional<Address> findById(AddressId addressId) {
        return addressJpaRepository.findById(addressId.getValue())
                .map(addressDataAccessMapper::addressEntityToAddress);
    }

    @Override
    public java.util.List<Address> findByUserId(com.berkay.identity.service.domain.valueobject.UserId userId) {
        return addressJpaRepository.findByUserId(userId.getValue()).stream()
                .map(addressDataAccessMapper::addressEntityToAddress)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void deleteById(AddressId addressId) {
        addressJpaRepository.deleteById(addressId.getValue());
    }
}
