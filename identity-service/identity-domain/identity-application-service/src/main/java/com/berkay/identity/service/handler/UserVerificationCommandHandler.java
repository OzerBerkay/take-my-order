package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.command.VerifyEmailCommand;
import com.berkay.identity.service.dto.command.VerifyPhoneCommand;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserVerificationCommandHandler {

    private final UserRepository userRepository;

    @Transactional
    public void verifyEmail(VerifyEmailCommand command) {
        User user = userRepository.findByEmail(command.getEmail())
                .orElseThrow(() -> new IdentityDomainException("User not found with email: " + command.getEmail()));

        // TODO: Kod doğrulama logic'i (Redis/Keycloak/DB check) buraya gelecek. Şimdilik kod doğru kabul edip ilerliyoruz.

        user.verifyEmail(); // Domain Status: ACTIVE olabilir.
        userRepository.save(user); // DB Güncelle

        log.info("Email verified for user: {}", user.getId().getValue());
    }

    @Transactional
    public void verifyPhone(VerifyPhoneCommand command) {
        User user = userRepository.findByPhoneNumber(command.getPhoneNumber())
                .orElseThrow(() -> new IdentityDomainException("User not found with phone: " + command.getPhoneNumber()));

        // TODO: SMS Kod doğrulama logic'i...

        user.verifyPhoneNumber();
        userRepository.save(user);

        log.info("Phone verified for user: {}", user.getId().getValue());
    }

}