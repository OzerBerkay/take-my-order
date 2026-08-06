package com.berkay.identity.service.dataaccess.adapter;

import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRevocationAdapter implements TokenRevocationPort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Based on the new design logic:
    // key: userId
    // value: {"type": "access_token", "iat": <current_time_in_seconds>}
    // TTL: 5 minutes (300 seconds)
    @Override
    public void revokeAccessToken(UUID userId) {
        String key = "user:" + userId + ":revoke";
        long iat = System.currentTimeMillis() / 1000;
        String value = "{\"type\":\"access_token\", \"iat\":" + iat + "}";
        
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(5));
            log.info("Revoked access token for user {}. Added to Redis with 5 min TTL.", userId);
        } catch (Exception e) {
            log.error("Failed to add access token revocation to Redis for user {}", userId, e);
        }
    }

    // Based on the new design logic:
    // key: userId
    // value: {"type": "all_tokens", "iat": <current_time_in_seconds>}
    // TTL: 7 days (604800 seconds)
    @Override
    public void revokeAllTokens(UUID userId) {
        String key = "user:" + userId + ":revoke";
        long iat = System.currentTimeMillis() / 1000;
        String value = "{\"type\":\"all_tokens\", \"iat\":" + iat + "}";
        
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofDays(7));
            log.info("Revoked all tokens for user {}. Added to Redis with 7 days TTL.", userId);
        } catch (Exception e) {
            log.error("Failed to add all tokens revocation to Redis for user {}", userId, e);
        }
    }

    // Based on the new design logic:
    // key: sid:{sessionId}:revoke
    // value: {"type": "single_logout"}
    // TTL: 7 days
    @Override
    public void revokeTokenBySessionId(String sessionId) {
        String key = "sid:" + sessionId + ":revoke";
        String value = "{\"type\":\"single_logout\"}";
        
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofDays(7));
            log.info("Revoked session {}. Added to Redis with 7 days TTL.", sessionId);
        } catch (Exception e) {
            log.error("Failed to add session revocation to Redis for session {}", sessionId, e);
        }
    }

    @Override
    public String checkTokenRevocation(String userId, String sid, long iat) {
        String userKey = "user:" + userId + ":revoke";
        String sidKey = "sid:" + sid + ":revoke";

        try {
            List<String> values = redisTemplate.opsForValue().multiGet(Arrays.asList(userKey, sidKey));
            
            if (values == null) {
                return null;
            }

            String userRevocation = values.get(0);
            String sidRevocation = values.get(1);

            if (sidRevocation != null) {
                JsonNode sidNode = objectMapper.readTree(sidRevocation);
                if ("single_logout".equals(sidNode.path("type").asText())) {
                    log.warn("Refresh token is blocked due to single_logout for sid: {}", sid);
                    return "ALL_TOKENS_REVOKED";
                }
            }

            if (userRevocation != null) {
                JsonNode userNode = objectMapper.readTree(userRevocation);
                String type = userNode.path("type").asText();
                long revokedIat = userNode.path("iat").asLong(0);

                if (("all_tokens".equals(type) || "access_token".equals(type)) && iat <= (revokedIat + 5)) {
                    log.warn("Refresh token is blocked due to {} for userId: {}, iat: {}, revokedIat: {}", type, userId, iat, revokedIat);
                    return "all_tokens".equals(type) ? "ALL_TOKENS_REVOKED" : "ACCESS_TOKEN_REVOKED";
                } else {
                    log.info("Token not blocked for userRevocation. type: {}, iat: {}, revokedIat: {}", type, iat, revokedIat);
                }
            } else {
                log.info("userRevocation is null for userId: {}", userId);
            }

        } catch (Exception e) {
            log.error("Error checking token revocation status in Redis for userId: {}, sid: {}", userId, sid, e);
        }

        return null;
    }
}
