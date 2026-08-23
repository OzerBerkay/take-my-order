package com.berkay.application.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class JwtSecurityFilterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @InjectMocks
    private JwtSecurityFilter jwtSecurityFilter;

    @BeforeEach
    void setUp() {
        jwtSecurityFilter = new JwtSecurityFilter(objectMapper, redisTemplate, handlerExceptionResolver, jwtDecoder);
        ReflectionTestUtils.setField(jwtSecurityFilter, "applicationName", "test-service");
        org.mockito.Mockito.lenient().when(jwtDecoder.decode(anyString())).thenReturn(org.mockito.Mockito.mock(Jwt.class));
    }
    
    @Test
    void shouldAssignM2mUserTypeWhenTokenHasNoInternalIdButHasValidClientId() throws Exception {
        // Arrange
        long validTime = (System.currentTimeMillis() / 1000) + 3600; 

        String payloadJson = "{" +
                "\"sub\":\"take-my-order-client\"," +
                "\"exp\":" + validTime + "," +
                "\"clientId\":\"take-my-order-client\"" +
                "}";

        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());
        String token = header + "." + payload + ".signature";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        JsonNode mockNode = new ObjectMapper().readTree(payloadJson);
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        // Act
        jwtSecurityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        org.junit.jupiter.api.Assertions.assertNotNull(auth);
        org.junit.jupiter.api.Assertions.assertTrue(auth instanceof JwtAuthenticationToken);
        
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        org.junit.jupiter.api.Assertions.assertEquals("M2M", jwtAuth.getUserType());
        org.junit.jupiter.api.Assertions.assertNotNull(jwtAuth.getInternalId()); // A random UUID should have been assigned
        
        // Clean up Context
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotAssignM2mUserTypeForOtherClients() throws Exception {
        // Arrange
        long validTime = (System.currentTimeMillis() / 1000) + 3600; 

        String payloadJson = "{" +
                "\"sub\":\"another-client\"," +
                "\"exp\":" + validTime + "," +
                "\"clientId\":\"another-client\"" +
                "}";

        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());
        String token = header + "." + payload + ".signature";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        JsonNode mockNode = new ObjectMapper().readTree(payloadJson);
        org.mockito.Mockito.lenient().when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        // Act
        jwtSecurityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        // Since there is no internal_id and it's not the whitelisted client, authentication should be null
        org.junit.jupiter.api.Assertions.assertNull(auth);
        
        // Clean up Context
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPrioritizeInternalIdOverClientId() throws Exception {
        // Arrange
        long validTime = (System.currentTimeMillis() / 1000) + 3600; 
        String internalId = java.util.UUID.randomUUID().toString();

        // Even if take-my-order-client is present, if internal_id exists, it's a real user.
        String payloadJson = "{" +
                "\"sub\":\"1234\"," +
                "\"exp\":" + validTime + "," +
                "\"internal_id\":\"" + internalId + "\"," +
                "\"clientId\":\"take-my-order-client\"," +
                "\"user_type\":\"CUSTOMER\"" +
                "}";

        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());
        String token = header + "." + payload + ".signature";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        JsonNode mockNode = new ObjectMapper().readTree(payloadJson);
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        // Act
        jwtSecurityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        org.junit.jupiter.api.Assertions.assertNotNull(auth);
        
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        org.junit.jupiter.api.Assertions.assertEquals("CUSTOMER", jwtAuth.getUserType()); // Not M2M!
        org.junit.jupiter.api.Assertions.assertEquals(java.util.UUID.fromString(internalId), jwtAuth.getInternalId());
        
        // Clean up Context
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        // Build an expired token payload
        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes());
        long expiredTime = (System.currentTimeMillis() / 1000) - 3600; // 1 hour ago
        String payloadJson = "{\"sub\":\"1234\", \"exp\":" + expiredTime + "}";
        String payload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());
        String signature = "dummy_signature";
        String token = header + "." + payload + "." + signature;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        JsonNode mockNode = new ObjectMapper().readTree(payloadJson);
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        jwtSecurityFilter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), any(com.berkay.application.exception.TokenExpiredException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldHandleTokenRevokedExceptionViaResolver() throws Exception {
        // Arrange
        long validTime = (System.currentTimeMillis() / 1000) + 3600; 
        long tokenIat = (System.currentTimeMillis() / 1000) - 100;
        String internalId = java.util.UUID.randomUUID().toString();
        String sid = "session-123";

        String payloadJson = "{" +
                "\"sub\":\"1234\"," +
                "\"exp\":" + validTime + "," +
                "\"iat\":" + tokenIat + "," +
                "\"internal_id\":\"" + internalId + "\"," +
                "\"sid\":\"" + sid + "\"," +
                "\"user_type\":\"MERCHANT\"" +
                "}";

        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());
        String token = header + "." + payload + ".signature";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        ObjectMapper realMapper = new ObjectMapper();
        JsonNode mockNode = realMapper.readTree(payloadJson);
        when(objectMapper.readTree(anyString())).thenReturn(mockNode).thenReturn(realMapper.readTree("{\"type\":\"access_token\", \"iat\":" + (tokenIat + 50) + "}"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // Return a mocked Redis response where the user is revoked
        String userRevokeJson = "{\"type\":\"access_token\", \"iat\":" + (tokenIat + 50) + "}";
        when(valueOperations.multiGet(any())).thenReturn(java.util.Arrays.asList(userRevokeJson, null));

        // Act
        jwtSecurityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), any(com.berkay.application.exception.TokenRevokedException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldRejectBannedUser() throws Exception {
        // Arrange
        long validTime = (System.currentTimeMillis() / 1000) + 3600; 
        String internalId = java.util.UUID.randomUUID().toString();

        // Simulate a token payload with account_status = ["BANNED"]
        String payloadJson = "{" +
                "\"sub\":\"1234\"," +
                "\"exp\":" + validTime + "," +
                "\"internal_id\":\"" + internalId + "\"," +
                "\"account_status\":[\"BANNED\"]," +
                "\"user_type\":\"CUSTOMER\"" +
                "}";

        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());
        String token = header + "." + payload + ".signature";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        JsonNode mockNode = new ObjectMapper().readTree(payloadJson);
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        // Act
        jwtSecurityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Account is BANNED");
        verify(filterChain, never()).doFilter(any(), any());
    }
}
