package com.berkay.identity.service.handler.helper;

import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreateHelper {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public void checkUserUniqueness(String email, String phoneNumber) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.error("User with email: {} already exists!", email);
            throw new IdentityDomainException("User with email " + email + " already exists!");
        }
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            log.error("User with phone: {} already exists!", phoneNumber);
            throw new IdentityDomainException("User with phone number " + phoneNumber + " already exists!");
        }
    }
}
