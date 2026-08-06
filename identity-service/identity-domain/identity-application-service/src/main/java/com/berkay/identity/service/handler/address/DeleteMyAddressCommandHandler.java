package com.berkay.identity.service.handler.address;

import com.berkay.identity.service.domain.entity.Address;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.AddressId;
import com.berkay.identity.service.ports.output.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMyAddressCommandHandler {

    private final AddressRepository addressRepository;

    @Transactional
    public void deleteMyAddress(UUID userId, UUID addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);
        
        Address addressToRemove = addressRepository.findById(new AddressId(addressId))
                .orElseThrow(() -> new IdentityDomainException("Address not found or does not belong to user"));

        if (!addressToRemove.getUserId().getValue().equals(userId)) {
            throw new IdentityDomainException("Address not found or does not belong to user");
        }
        
        addressRepository.deleteById(new AddressId(addressId));
        log.info("Address {} successfully deleted for user {}", addressId, userId);
    }
}
