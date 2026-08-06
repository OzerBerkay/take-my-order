package com.berkay.identity.service.ports.output.repository;

import java.util.UUID;

public interface TokenRevocationPort {
    
    void revokeAccessToken(UUID userId);
    
    void revokeAllTokens(UUID userId);
    
    void revokeTokenBySessionId(String sessionId);
    
    String checkTokenRevocation(String userId, String sid, long iat);
}
