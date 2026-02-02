package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.command.VerifyEmailCommand;
import com.berkay.identity.service.dto.command.VerifyPhoneCommand;
import com.berkay.identity.service.mapper.UserDataMapper;
import com.berkay.identity.service.outbox.model.DomainEventType;
import com.berkay.identity.service.outbox.model.UserEventPayload;
import com.berkay.identity.service.outbox.scheduler.UserOutboxHelper;
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
    private final UserDataMapper userDataMapper;
    private final UserOutboxHelper userOutboxHelper;

    @Transactional
    public void verifyEmail(VerifyEmailCommand command) {
        User user = userRepository.findByEmail(command.getEmail())
                .orElseThrow(() -> new IdentityDomainException("User not found with email: " + command.getEmail()));

        // TODO: Kod doğrulama logic'i (Redis/Keycloak/DB check) buraya gelecek. Şimdilik kod doğru kabul edip ilerliyoruz.

        user.verifyEmail(); // Domain Status: ACTIVE olabilir.
        userRepository.save(user); // DB Güncelle

        if (user.getUserType() != UserType.INTERNAL) {
            log.info("Saving Outbox Message for user update (Email Verified): {}", user.getId().getValue());
            saveOutboxMessage(user);
        }

        log.info("Email verified for user: {}", user.getId().getValue());
    }

    @Transactional
    public void verifyPhone(VerifyPhoneCommand command) {
        User user = userRepository.findByPhoneNumber(command.getPhoneNumber())
                .orElseThrow(() -> new IdentityDomainException("User not found with phone: " + command.getPhoneNumber()));

        // TODO: SMS Kod doğrulama logic'i...

        user.verifyPhoneNumber();
        userRepository.save(user);

        if (user.getUserType() != UserType.INTERNAL) {
            log.info("Saving Outbox Message for user update (Phone Verified): {}", user.getId().getValue());
            saveOutboxMessage(user);
        }

        log.info("Phone verified for user: {}", user.getId().getValue());
    }

    private void saveOutboxMessage(User user) {
        // Create ile aynı payload yapısını kullanıyoruz (Snapshot)
        UserEventPayload payload = userDataMapper.userToUserEventPayload(user);

        // Helper'a tipi parametre olarak geçiyoruz: USER_UPDATED
        userOutboxHelper.saveUserOutboxMessage(payload, DomainEventType.USER_UPDATED);
    }

}