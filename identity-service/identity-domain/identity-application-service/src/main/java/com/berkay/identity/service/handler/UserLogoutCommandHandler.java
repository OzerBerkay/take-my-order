package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.dto.command.TokenRevocationResponse;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.security.SecurityContextPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserLogoutCommandHandler {

    private final TokenRevocationPort tokenRevocationPort;
    private final SecurityContextPort securityContextPort;

    public TokenRevocationResponse logout() {
        String sessionId = securityContextPort.getCurrentSessionId();
        UUID userId = securityContextPort.getCurrentInternalUserId();
        
        log.info("User {} is requesting single logout for session: {}", userId, sessionId);

        if (sessionId == null || sessionId.isBlank()) {
            throw new IdentityDomainException("Cannot perform single logout because session ID is missing from the token.");
        }

        tokenRevocationPort.revokeTokenBySessionId(sessionId);

        log.info("Successfully completed single logout for user {} session {}", userId, sessionId);
        
        return TokenRevocationResponse.builder()
                .userId(userId)
                .message("Successfully logged out from the current device")
                .build();
    }

    public TokenRevocationResponse logoutAll() {
        UUID userId = securityContextPort.getCurrentInternalUserId();
        
        log.info("User {} is requesting global logout (all devices).", userId);

        tokenRevocationPort.revokeAllTokens(userId);

        log.info("Successfully completed global logout for user {}", userId);
        
        return TokenRevocationResponse.builder()
                .userId(userId)
                .message("Successfully logged out from all devices")
                .build();
    }
}
