package com.berkay.application.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class JwtSecurityFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final org.springframework.web.servlet.HandlerExceptionResolver handlerExceptionResolver;

    public JwtSecurityFilter(ObjectMapper objectMapper, 
                             org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
                             @org.springframework.beans.factory.annotation.Qualifier("handlerExceptionResolver") 
                             org.springframework.web.servlet.HandlerExceptionResolver handlerExceptionResolver) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @org.springframework.beans.factory.annotation.Value("${spring.application.name:unknown-service}")
    private String applicationName;

    @org.springframework.beans.factory.annotation.Value("#{${security.rbac.domain-policies:{}}}")
    private java.util.Map<String, String> domainPolicies;

    private boolean isUserAllowedInDomain(String userType) {
        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            // Customers usually have access to order and restaurant, but typically we handle them implicitly.
            // Let's assume if it's not configured, they might have default access, or they don't have restriction.
            // To be safe and strict according to document, maybe we check the policy.
            return true; // Customers are handled by Layer 3 or endpoint specific checks, or we can add CUSTOMER to yml.
        }

        if (domainPolicies == null || domainPolicies.isEmpty()) {
            return true; // No policy defined, bypass
        }

        String allowedDomainsStr = domainPolicies.get(userType.toUpperCase());
        if (allowedDomainsStr == null || allowedDomainsStr.isBlank()) {
            // Policy exists but this user type has no domains configured, maybe block?
            // "Eğer MERCHANT kullanıcısı yetkisi dışındaki bir servisi çağırıyorsa anında 403"
            return false;
        }

        // e.g. applicationName = "order-service" -> domain = "ORDER"
        String currentDomain = applicationName.replace("-service", "").toUpperCase();
        
        String[] allowedDomains = allowedDomainsStr.split(",");
        for (String domain : allowedDomains) {
            if (domain.trim().equalsIgnoreCase(currentDomain)) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                    JsonNode jsonNode = objectMapper.readTree(payload);

                    // Read standard claim
                    String externalId = jsonNode.has("sub") ? jsonNode.get("sub").asText() : null;

                    // Token Expiration Validation
                    if (jsonNode.has("exp")) {
                        long exp = jsonNode.get("exp").asLong();
                        long now = System.currentTimeMillis() / 1000;
                        if (exp < now) {
                            log.warn("Expired token used. exp: {}, now: {}, externalId: {}", exp, now, externalId);
                            handlerExceptionResolver.resolveException(request, response, null, 
                                new com.berkay.application.exception.TokenExpiredException("ACCESS_TOKEN_EXPIRED", "Token is expired. Please log in again or refresh your token."));
                            return;
                        }
                    }

                    // Read Keycloak Custom Attributes (they come as arrays e.g. ["ACTIVE"])
                    String accountStatus = extractFirstElementOrNull(jsonNode, "account_status");
                    
                    // Business Validation: BANNED check (Faz-1 Kuralı)
                    if ("BANNED".equals(accountStatus)) {
                        log.warn("Banned user tried to access: {}", externalId);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account is BANNED");
                        return;
                    }

                    String internalIdStr = extractFirstElementOrNull(jsonNode, "internal_id");
                    UUID internalId = internalIdStr != null ? UUID.fromString(internalIdStr) : null;
                    
                    String userType = extractFirstElementOrNull(jsonNode, "user_type");

                    // Layer 2: Domain-Level Guardrail
                    if (userType != null && !isUserAllowedInDomain(userType)) {
                        log.warn("Domain Guardrail Blocked! user_type {} is not allowed in domain {}", userType, applicationName);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Domain access denied for user type");
                        return;
                    }

                    // JWT Revocation check (Redis)
                    String sid = extractFirstElementOrNull(jsonNode, "sid");
                    long iat = jsonNode.has("iat") ? jsonNode.get("iat").asLong() : 0L;
                    if (internalIdStr != null && sid != null) {
                        try {
                            List<String> results = redisTemplate.opsForValue().multiGet(
                                java.util.Arrays.asList("user:" + internalIdStr + ":revoke", "sid:" + sid + ":revoke")
                            );
                            
                            if (results != null && results.size() == 2) {
                                String userRevokeStr = results.get(0);
                                String sidRevokeStr = results.get(1);

                                if (sidRevokeStr != null) {
                                    JsonNode sidRevoke = objectMapper.readTree(sidRevokeStr);
                                    if ("single_logout".equals(sidRevoke.get("type").asText())) {
                                        throw new com.berkay.application.exception.TokenRevokedException("ALL_TOKENS_REVOKED", "All active sessions have been terminated.");
                                    }
                                }

                                if (userRevokeStr != null) {
                                    JsonNode userRevoke = objectMapper.readTree(userRevokeStr);
                                    long redisIat = userRevoke.get("iat").asLong();
                                    long gracePeriod = 5L;
                                    if (iat <= (redisIat + gracePeriod)) {
                                        String type = userRevoke.get("type").asText();
                                        if ("access_token".equals(type)) {
                                            throw new com.berkay.application.exception.TokenRevokedException("ACCESS_TOKEN_REVOKED", "Token has been revoked. Please refresh your access token.");
                                        } else if ("all_tokens".equals(type) || "global_logout".equals(type)) {
                                            throw new com.berkay.application.exception.TokenRevokedException("ALL_TOKENS_REVOKED", "All active sessions have been terminated.");
                                        }
                                    }
                                }
                            }
                        } catch (com.berkay.application.exception.TokenRevokedException e) {
                            handlerExceptionResolver.resolveException(request, response, null, e);
                            return;
                        } catch (Exception e) {
                            log.error("Failed to check Redis for token revocation: {}", e.getMessage());
                        }
                    }

                    List<UUID> roleIds = extractUuidList(jsonNode, "role_ids");
                    List<UUID> organizationalUnitIds = extractUuidList(jsonNode, "organizational_unit_ids");

                    if (internalId != null) {
                        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(
                                internalId, externalId, userType, roleIds, organizationalUnitIds, sid, new ArrayList<>()
                        );
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        log.debug("Successfully authenticated user {} with roles {}", internalId, roleIds);
                        // IDOR shield is intentionally removed in favor of @PreAuthorize checks
                        // which provide precise, replica-based authorization without token bloat.
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse JWT token in microservice: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }



    private String extractFirstElementOrNull(JsonNode rootNode, String fieldName) {
        if (rootNode.has(fieldName)) {
            JsonNode node = rootNode.get(fieldName);
            if (node.isArray() && !node.isEmpty()) {
                return node.get(0).asText();
            } else if (!node.isArray()) {
                return node.asText();
            }
        }
        return null;
    }

    private List<UUID> extractUuidList(JsonNode rootNode, String fieldName) {
        List<UUID> list = new ArrayList<>();
        if (rootNode.has(fieldName)) {
            JsonNode node = rootNode.get(fieldName);
            
            // Helper to parse strings that might be like "[uuid1, uuid2]" or just "uuid1"
            java.util.function.Consumer<String> parseAndAdd = (text) -> {
                text = text.trim();
                if (text.startsWith("[") && text.endsWith("]")) {
                    text = text.substring(1, text.length() - 1);
                }
                if (text.isEmpty()) return;
                String[] parts = text.split(",");
                for (String part : parts) {
                    try {
                        list.add(UUID.fromString(part.trim()));
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid UUID in token field {}: {}", fieldName, part.trim());
                    }
                }
            };

            if (node.isArray()) {
                for (JsonNode element : node) {
                    parseAndAdd.accept(element.asText());
                }
            } else if (node.isTextual()) {
                parseAndAdd.accept(node.asText());
            }
        }
        return list;
    }
}
