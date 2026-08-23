package com.berkay.identity.service.application.security.jwt;

import com.berkay.identity.service.domain.valueobject.AccountStatus;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final org.springframework.web.servlet.HandlerExceptionResolver handlerExceptionResolver;
    private final org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    public JwtAuthenticationFilter(ObjectMapper objectMapper, 
                                   org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
                                   @org.springframework.beans.factory.annotation.Qualifier("handlerExceptionResolver") 
                                   org.springframework.web.servlet.HandlerExceptionResolver handlerExceptionResolver,
                                   @org.springframework.beans.factory.annotation.Autowired(required = false) org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtDecoder != null) {
                    try {
                        org.springframework.security.oauth2.jwt.Jwt jwt = jwtDecoder.decode(token);
                        log.debug("JWT Signature successfully verified for user: {}", jwt.getSubject());
                    } catch (org.springframework.security.oauth2.jwt.JwtException e) {
                        log.warn("JWT Signature verification failed: {}", e.getMessage());
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Signature");
                        return;
                    }
                } else {
                    log.warn("JwtDecoder is not configured! Skipping signature verification.");
                }

                String[] chunks = token.split("\\.");
                if (chunks.length > 1) {
                    String payloadJson = new String(Base64.getUrlDecoder().decode(chunks[1]));
                    JsonNode payload = objectMapper.readTree(payloadJson);

                    // Token Expiration Validation
                    if (payload.hasNonNull("exp")) {
                        long exp = payload.get("exp").asLong();
                        long now = System.currentTimeMillis() / 1000;
                        if (exp < now) {
                            log.warn("Expired token used. exp: {}, now: {}, externalId: {}", exp, now, payload.get("sub").asText());
                            handlerExceptionResolver.resolveException(request, response, null, 
                                new com.berkay.identity.service.domain.exception.TokenExpiredDomainException("ACCESS_TOKEN_EXPIRED", "Token is expired. Please log in again or refresh your token."));
                            return;
                        }
                    }

                    String clientId = payload.hasNonNull("clientId") ? payload.get("clientId").asText() : 
                                     (payload.hasNonNull("azp") ? payload.get("azp").asText() : null);
                    
                    boolean isM2M = !payload.hasNonNull("internal_id") && "take-my-order-client".equals(clientId);

                    // Token Validation (Check if all required custom claims exist)
                    if (!isM2M && (!payload.hasNonNull("sub") || 
                        (!payload.hasNonNull("account_status") && !payload.hasNonNull("accountStatus")) || 
                        !payload.hasNonNull("internal_id") || 
                        !payload.hasNonNull("user_type"))) {
                        log.error("JWT Token is missing critical custom claims! This might be an old token or a token generated before mapping fixes.");
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token Payload: Missing custom claims.");
                        return;
                    }

                    AccountStatus accountStatus = AccountStatus.ACTIVE;
                    UUID externalId = null;
                    UUID internalId = null;
                    UserType userType = UserType.M2M;
                    String email = "";

                    if (!isM2M) {
                        // 1. Döküman Request Flow Adım 3: Account Status Kontrolü (BANNED Check)
                        String statusString = payload.hasNonNull("account_status") ? payload.get("account_status").asText() : payload.get("accountStatus").asText();
                        accountStatus = AccountStatus.valueOf(statusString);
                        if (AccountStatus.BANNED.equals(accountStatus)) {
                            log.warn("BANNED user tried to access! External ID: {}", payload.get("sub").asText());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account is BANNED!");
                            return; // Filtreyi kes, içeri alma!
                        }

                        // 2. Claim'leri Oku
                        externalId = UUID.fromString(payload.get("sub").asText());
                        internalId = UUID.fromString(payload.get("internal_id").asText());
                        userType = UserType.valueOf(payload.get("user_type").asText());
                        email = payload.hasNonNull("email") ? payload.get("email").asText() : "";
                    } else {
                        // M2M Mock values
                        externalId = UUID.nameUUIDFromBytes(clientId.getBytes());
                        internalId = externalId;
                    }

                    List<UUID> roleIds = new ArrayList<>();
                    if (payload.hasNonNull("role_ids") && payload.get("role_ids").isArray()) {
                        payload.get("role_ids").forEach(node -> roleIds.add(UUID.fromString(node.asText())));
                    }

                    List<UUID> orgUnitIds = new ArrayList<>();
                    if (payload.hasNonNull("organizational_unit_ids") && payload.get("organizational_unit_ids").isArray()) {
                        payload.get("organizational_unit_ids").forEach(node -> orgUnitIds.add(UUID.fromString(node.asText())));
                    }

                    String sid = payload.hasNonNull("sid") ? payload.get("sid").asText() : null;

                    // Token Revocation Check
                    if (payload.hasNonNull("iat") && sid != null) {
                        long iat = payload.get("iat").asLong();
                        String userIdStr = internalId.toString();

                        String sidKey = "sid:" + sid + ":revoke";
                        String userKey = "user:" + userIdStr + ":revoke";
                        
                        log.info("Checking Redis for token revocation. sidKey: {}, userKey: {}, iat: {}", sidKey, userKey, iat);

                        List<String> values = redisTemplate.opsForValue().multiGet(List.of(sidKey, userKey));

                        if (values != null) {
                            String sidVal = values.get(0);
                            String userVal = values.get(1);
                            
                            log.info("Redis returned: sidVal={}, userVal={}", sidVal, userVal);

                            if (sidVal != null) {
                                log.warn("Token revoked due to single logout. SID: {}", sid);
                                handlerExceptionResolver.resolveException(request, response, null, 
                                    new com.berkay.identity.service.domain.exception.TokenRevokedDomainException("ALL_TOKENS_REVOKED", "All active sessions have been terminated."));
                                return;
                            }

                            if (userVal != null) {
                                try {
                                    JsonNode userNode = objectMapper.readTree(userVal);
                                    long revokedIat = userNode.get("iat").asLong();
                                    long gracePeriod = 5L;
                                    if (iat <= (revokedIat + gracePeriod)) {
                                        String revokeType = userNode.get("type").asText();
                                        log.warn("Token revoked globally. User: {}", userIdStr);
                                        
                                        if ("access_token".equals(revokeType)) {
                                            handlerExceptionResolver.resolveException(request, response, null, 
                                                new com.berkay.identity.service.domain.exception.TokenRevokedDomainException("ACCESS_TOKEN_REVOKED", "Token has been revoked. Please refresh your access token."));
                                        } else {
                                            handlerExceptionResolver.resolveException(request, response, null, 
                                                new com.berkay.identity.service.domain.exception.TokenRevokedDomainException("ALL_TOKENS_REVOKED", "All active sessions have been terminated."));
                                        }
                                        return;
                                    }
                                } catch (Exception e) {
                                    log.error("Failed to parse user revoke JSON", e);
                                }
                            }
                        }
                    }

                    // 3. Spring Security Context'ine Yerleştir
                    JwtAuthenticationToken authToken = new JwtAuthenticationToken(
                            externalId, internalId, userType, accountStatus, email, roleIds, orgUnitIds, sid, token
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                log.error("Could not parse JWT token!", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}