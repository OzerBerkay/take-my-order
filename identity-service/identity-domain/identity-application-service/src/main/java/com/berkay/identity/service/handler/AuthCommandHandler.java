package com.berkay.identity.service.handler;

import com.berkay.identity.service.dto.command.LoginCommand;
import com.berkay.identity.service.dto.command.RefreshTokenCommand;
import com.berkay.identity.service.dto.command.TokenResponse;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.domain.exception.TokenRevokedDomainException;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.AccountStatus;
import com.berkay.identity.service.domain.valueobject.UserEmail;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthCommandHandler {

    private final IdentityProviderPort identityProviderPort;
    private final TokenRevocationPort tokenRevocationPort;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginCommand command) {
        Optional<User> userOptional = userRepository.findByEmail(command.getEmail());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getStatus() == AccountStatus.BANNED) {
                log.warn("Banned user attempted to login: {}", command.getEmail());
                throw new IdentityDomainException("Your account has been banned.");
            }
        }
        
        return identityProviderPort.login(command.getEmail(), command.getPassword());
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshTokenCommand command) {
        String token = command.getRefreshToken();
        
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                JsonNode rootNode = objectMapper.readTree(payload);
                
                String sid = rootNode.path("sid").asText(null);
                String internalId = rootNode.path("internal_id").asText(null);
                
                if (internalId == null) {
                    String sub = rootNode.path("sub").asText(null);
                    if (sub != null) {
                        internalId = userRepository.findByExternalId(sub).map(u -> u.getId().getValue().toString()).orElse(null);
                    }
                }
                
                long iat = rootNode.path("iat").asLong(0);

                String revokeCode = tokenRevocationPort.checkTokenRevocation(internalId, sid, iat);
                log.info("CheckTokenRevocation result - internalId: {}, sid: {}, iat: {}, revokeCode: {}", internalId, sid, iat, revokeCode);
                if ("ALL_TOKENS_REVOKED".equals(revokeCode)) {
                    log.warn("Attempt to refresh with revoked token! internalId: {}, sid: {}, code: {}", internalId, sid, revokeCode);
                    throw new TokenRevokedDomainException(revokeCode, "Refresh token is revoked!");
                }
            }
        } catch (TokenRevokedDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error parsing refresh token during revocation check", e);
            // We do not block if it's just a parsing error; Keycloak will validate it.
        }

        return identityProviderPort.refreshToken(token);
    }
}
