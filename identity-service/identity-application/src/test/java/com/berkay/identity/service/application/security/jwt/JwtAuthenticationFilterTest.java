package com.berkay.identity.service.application.security.jwt;

import com.berkay.identity.service.domain.valueobject.AccountStatus;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    private ObjectMapper objectMapper;
    
    @Mock
    private StringRedisTemplate redisTemplate;
    
    private ValueOperations<String, String> valueOperations;
    
    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private JwtDecoder jwtDecoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(objectMapper, redisTemplate, handlerExceptionResolver, jwtDecoder);
        org.mockito.Mockito.lenient().when(jwtDecoder.decode(anyString())).thenReturn(org.mockito.Mockito.mock(org.springframework.security.oauth2.jwt.Jwt.class));
        SecurityContextHolder.clearContext();
    }

    private String createDummyToken(String sid, long iat, String internalId, String externalId, String accountStatus) {
        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = String.format("{\"sub\":\"%s\", \"internal_id\":\"%s\", \"user_type\":\"MERCHANT\", \"account_status\":\"%s\", \"sid\":\"%s\", \"iat\":%d}", 
                externalId, internalId, accountStatus, sid, iat);
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
        return header + "." + encodedPayload + ".dummySignature";
    }

    @Test
    public void testValidToken_ShouldAuthenticate() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        UUID internalId = UUID.randomUUID();
        UUID externalId = UUID.randomUUID();
        String sid = UUID.randomUUID().toString();
        long iat = System.currentTimeMillis() / 1000;

        String token = createDummyToken(sid, iat, internalId.toString(), externalId.toString(), "ACTIVE");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(valueOperations.multiGet(anyList())).thenReturn(java.util.Arrays.asList((String) null, (String) null)); // null is required to fix ambiguity

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken);
        
        JwtAuthenticationToken authToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertEquals(internalId, authToken.getInternalId());
        assertEquals(externalId, authToken.getExternalId());
        assertEquals(sid, authToken.getSid());
    }

    @Test
    public void testSingleLogoutToken_ShouldReturn401() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        UUID internalId = UUID.randomUUID();
        UUID externalId = UUID.randomUUID();
        String sid = UUID.randomUUID().toString();
        long iat = System.currentTimeMillis() / 1000;

        String token = createDummyToken(sid, iat, internalId.toString(), externalId.toString(), "ACTIVE");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(valueOperations.multiGet(anyList())).thenReturn(java.util.Arrays.asList("{\"type\":\"single_logout\"}", null));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(com.berkay.identity.service.domain.exception.TokenRevokedDomainException.class));
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testGlobalLogoutToken_ShouldReturn401() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        UUID internalId = UUID.randomUUID();
        UUID externalId = UUID.randomUUID();
        String sid = UUID.randomUUID().toString();
        long iat = 1000; // token iat
        long revokedIat = 1005; // revoked later

        String token = createDummyToken(sid, iat, internalId.toString(), externalId.toString(), "ACTIVE");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(valueOperations.multiGet(anyList())).thenReturn(java.util.Arrays.asList(null, "{\"type\":\"global_logout\", \"iat\":" + revokedIat + "}"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(com.berkay.identity.service.domain.exception.TokenRevokedDomainException.class));
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    public void testGlobalLogoutToken_WithClockSkewGracePeriod_ShouldReturn401() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        UUID internalId = UUID.randomUUID();
        UUID externalId = UUID.randomUUID();
        String sid = UUID.randomUUID().toString();
        
        // Token was created at 1005 (by Keycloak)
        long iat = 1005; 
        // Identity service recorded global logout at 1000 (clock skew)
        long revokedIat = 1000; 

        // 1005 <= (1000 + 5) -> Should block it because it's within the grace period.

        String token = createDummyToken(sid, iat, internalId.toString(), externalId.toString(), "ACTIVE");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(valueOperations.multiGet(anyList())).thenReturn(java.util.Arrays.asList(null, "{\"type\":\"global_logout\", \"iat\":" + revokedIat + "}"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(com.berkay.identity.service.domain.exception.TokenRevokedDomainException.class));
        verify(filterChain, never()).doFilter(request, response);
    }
    
    @Test
    public void testGlobalLogoutToken_AfterGracePeriod_ShouldAuthenticate() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        UUID internalId = UUID.randomUUID();
        UUID externalId = UUID.randomUUID();
        String sid = UUID.randomUUID().toString();
        
        // Token was created at 1010
        long iat = 1010; 
        // Identity service recorded global logout at 1000
        long revokedIat = 1000; 

        // 1010 <= (1000 + 5) -> False. This token was obtained LEGITIMATELY after the logout + grace period.

        String token = createDummyToken(sid, iat, internalId.toString(), externalId.toString(), "ACTIVE");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(valueOperations.multiGet(anyList())).thenReturn(java.util.Arrays.asList(null, "{\"type\":\"global_logout\", \"iat\":" + revokedIat + "}"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private String createDummyTokenWithRoles(String sid, long iat, String internalId, String externalId, String accountStatus, List<UUID> roleIds) {
        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String roleIdsJson = roleIds.stream().map(r -> "\"" + r + "\"").reduce((a, b) -> a + "," + b).orElse("");
        String payload = String.format("{\"sub\":\"%s\", \"internal_id\":\"%s\", \"user_type\":\"MERCHANT\", \"account_status\":\"%s\", \"sid\":\"%s\", \"iat\":%d, \"role_ids\":[%s]}", 
                externalId, internalId, accountStatus, sid, iat, roleIdsJson);
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
        return header + "." + encodedPayload + ".dummySignature";
    }

    @Test
    public void testRoleUpdatedToken_WithinGracePeriod_ShouldReturn401() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        UUID internalId = UUID.randomUUID();
        UUID externalId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        String sid = UUID.randomUUID().toString();
        
        long revokedIat = 1000L;
        long tokenIat = 1003L; // <= 1000 + 5

        String token = createDummyTokenWithRoles(sid, tokenIat, internalId.toString(), externalId.toString(), "ACTIVE", List.of(roleId));
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        String roleRevocation = "{\"type\":\"role_updated\", \"iat\":" + revokedIat + "}";
        when(valueOperations.multiGet(anyList())).thenReturn(java.util.Arrays.asList(null, null, roleRevocation));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(
                eq(request), eq(response), isNull(),
                argThat(ex -> ex instanceof com.berkay.identity.service.domain.exception.TokenRevokedDomainException
                        && "ACCESS_TOKEN_REVOKED".equals(((com.berkay.identity.service.domain.exception.TokenRevokedDomainException) ex).getErrorCode()))
        );
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testRoleUpdatedToken_AfterGracePeriod_ShouldAuthenticate() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        UUID internalId = UUID.randomUUID();
        UUID externalId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        String sid = UUID.randomUUID().toString();
        
        long revokedIat = 1000L;
        long tokenIat = 1010L; // > 1000 + 5 (fresh token obtained after role update)

        String token = createDummyTokenWithRoles(sid, tokenIat, internalId.toString(), externalId.toString(), "ACTIVE", List.of(roleId));
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        String roleRevocation = "{\"type\":\"role_updated\", \"iat\":" + revokedIat + "}";
        when(valueOperations.multiGet(anyList())).thenReturn(java.util.Arrays.asList(null, null, roleRevocation));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
