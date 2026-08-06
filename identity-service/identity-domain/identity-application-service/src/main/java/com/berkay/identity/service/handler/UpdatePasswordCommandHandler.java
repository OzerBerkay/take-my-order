package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.UpdatePasswordCommand;
import com.berkay.identity.service.dto.command.ForceResetPasswordCommand;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePasswordCommandHandler {

    private final UserRepository userRepository;
    private final IdentityProviderPort identityProviderPort;
    private final com.berkay.identity.service.ports.output.security.SecurityContextPort securityContextPort;

    // SIFIR TRANSACTION KURALI: DB'de herhangi bir update yok, sadece Keycloak isteği var
    // Eğer şifre değişikliğini loglamak isteseydik transaction gerekirdi, şu an gerekmiyor.
    public void updatePassword(UpdatePasswordCommand command) {
        java.util.UUID currentUserId = securityContextPort.getCurrentInternalUserId();
        
        User user = userRepository.findById(new UserId(currentUserId))
                .orElseThrow(() -> new IdentityDomainException("User not found with id: " + currentUserId));

        identityProviderPort.updatePassword(user.getExternalId(), command.getNewPassword());
        log.info("Password updated successfully for user id: {}", user.getId().getValue());
    }

    public void forceResetPassword(ForceResetPasswordCommand command) {
        User user = userRepository.findById(new UserId(command.getUserId()))
                .orElseThrow(() -> new IdentityDomainException("User not found with id: " + command.getUserId()));

        identityProviderPort.resetPassword(user.getExternalId(), command.getNewPassword());
        log.info("Password force reset successfully for user id: {}", user.getId().getValue());
    }
}
