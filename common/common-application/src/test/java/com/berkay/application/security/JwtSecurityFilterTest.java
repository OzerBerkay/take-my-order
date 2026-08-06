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

    private JwtSecurityFilter jwtSecurityFilter;

    @BeforeEach
    void setUp() {
        jwtSecurityFilter = new JwtSecurityFilter(objectMapper, redisTemplate, handlerExceptionResolver);
        ReflectionTestUtils.setField(jwtSecurityFilter, "applicationName", "test-service");
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
    void shouldExtractOrganizationalUnitIdsEvenIfStringifiedArray() throws Exception {
        // Arrange
        long validTime = (System.currentTimeMillis() / 1000) + 3600; 
        String internalId = java.util.UUID.randomUUID().toString();
        String orgId1 = java.util.UUID.randomUUID().toString();
        String orgId2 = java.util.UUID.randomUUID().toString();

        // Simulate a token payload where organizational_unit_ids is a single String containing a JSON-like array
        String payloadJson = "{" +
                "\"sub\":\"1234\"," +
                "\"exp\":" + validTime + "," +
                "\"internal_id\":\"" + internalId + "\"," +
                "\"sid\":\"session-123\"," +
                "\"user_type\":\"MERCHANT\"," +
                "\"organizational_unit_ids\":\"[" + orgId1 + ", " + orgId2 + "]\"" +
                "}";

        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());
        String token = header + "." + payload + ".signature";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        JsonNode mockNode = new ObjectMapper().readTree(payloadJson);
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(any())).thenReturn(java.util.Arrays.asList(null, null));

        // Act
        jwtSecurityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        org.junit.jupiter.api.Assertions.assertNotNull(auth);
        org.junit.jupiter.api.Assertions.assertTrue(auth instanceof JwtAuthenticationToken);
        
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        java.util.List<java.util.UUID> orgUnits = jwtAuth.getOrganizationalUnitIds();
        
        org.junit.jupiter.api.Assertions.assertNotNull(orgUnits);
        org.junit.jupiter.api.Assertions.assertEquals(2, orgUnits.size());
        org.junit.jupiter.api.Assertions.assertTrue(orgUnits.contains(java.util.UUID.fromString(orgId1)));
        org.junit.jupiter.api.Assertions.assertTrue(orgUnits.contains(java.util.UUID.fromString(orgId2)));
        
        // Clean up Context
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
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
