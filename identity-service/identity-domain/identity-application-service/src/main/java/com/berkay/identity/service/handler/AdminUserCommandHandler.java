package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.TokenRevocationResponse;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserCommandHandler {

    private final UserRepository userRepository;
    private final TokenRevocationPort tokenRevocationPort;

    public TokenRevocationResponse revokeAccess(UUID targetUserId) {
        log.info("Admin revoking access token for user id: {}", targetUserId);

        userRepository.findById(new UserId(targetUserId))
                .orElseThrow(() -> new IdentityDomainException("User not found: " + targetUserId));

        tokenRevocationPort.revokeAccessToken(targetUserId);

        log.info("Admin successfully revoked access token for user id: {}", targetUserId);
        return TokenRevocationResponse.builder()
                .userId(targetUserId)
                .message("Access token successfully revoked")
                .build();
    }

    public TokenRevocationResponse revokeAll(UUID targetUserId) {
        log.info("Admin revoking all tokens for user id: {}", targetUserId);

        userRepository.findById(new UserId(targetUserId))
                .orElseThrow(() -> new IdentityDomainException("User not found: " + targetUserId));

        tokenRevocationPort.revokeAllTokens(targetUserId);

        log.info("Admin successfully revoked all tokens for user id: {}", targetUserId);
        return TokenRevocationResponse.builder()
                .userId(targetUserId)
                .message("All active tokens successfully revoked")
                .build();
    }
}
