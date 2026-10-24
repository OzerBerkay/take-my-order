package com.berkay.identity.service.dataaccess.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenRevocationAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;
    private TokenRevocationAdapter adapter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        adapter = new TokenRevocationAdapter(redisTemplate, objectMapper);
    }

    @Test
    @DisplayName("Rol iptali: revokeRole çağrıldığında Redis'e role:{roleId}:revoke anahtarı 5 dakika TTL ile yazılmalı")
    void shouldRevokeRoleWithFiveMinutesTtl() {
        UUID roleId = UUID.randomUUID();

        adapter.revokeRole(roleId);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

        verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture(), durationCaptor.capture());

        assertEquals("role:" + roleId + ":revoke", keyCaptor.getValue());
        assertTrue(valueCaptor.getValue().contains("\"type\":\"role_updated\""));
        assertTrue(valueCaptor.getValue().contains("\"iat\":"));
        assertEquals(Duration.ofMinutes(5), durationCaptor.getValue());
    }

    @Test
    @DisplayName("Kullanıcı erişim token iptali: revokeAccessToken çağrıldığında 5 dakika TTL ile yazılmalı")
    void shouldRevokeAccessToken() {
        UUID userId = UUID.randomUUID();

        adapter.revokeAccessToken(userId);

        verify(valueOperations).set(eq("user:" + userId + ":revoke"), contains("\"type\":\"access_token\""), eq(Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("Oturum iptali: revokeTokenBySessionId 7 gün TTL ile yazılmalı")
    void shouldRevokeTokenBySessionId() {
        String sessionId = UUID.randomUUID().toString();

        adapter.revokeTokenBySessionId(sessionId);

        verify(valueOperations).set(eq("sid:" + sessionId + ":revoke"), eq("{\"type\":\"single_logout\"}"), eq(Duration.ofDays(7)));
    }

    @Test
    @DisplayName("Refresh token kontrolü: Tekil oturum çıkışı (single_logout) varsa ALL_TOKENS_REVOKED dönmeli")
    void shouldReturnAllTokensRevoked_WhenSingleLogout() {
        String userId = UUID.randomUUID().toString();
        String sid = UUID.randomUUID().toString();

        when(valueOperations.multiGet(Arrays.asList("user:" + userId + ":revoke", "sid:" + sid + ":revoke")))
                .thenReturn(Arrays.asList(null, "{\"type\":\"single_logout\"}"));

        String result = adapter.checkTokenRevocation(userId, sid, 1000L);

        assertEquals("ALL_TOKENS_REVOKED", result);
    }

    @Test
    @DisplayName("Refresh token kontrolü: Kullanıcı all_tokens iptali ve grace period içindeyse ALL_TOKENS_REVOKED dönmeli")
    void shouldReturnAllTokensRevoked_WhenAllTokensRevokedWithinGracePeriod() {
        String userId = UUID.randomUUID().toString();
        String sid = UUID.randomUUID().toString();
        long revokedIat = 1000L;
        long tokenIat = 1003L; // <= 1000 + 5

        when(valueOperations.multiGet(Arrays.asList("user:" + userId + ":revoke", "sid:" + sid + ":revoke")))
                .thenReturn(Arrays.asList("{\"type\":\"all_tokens\",\"iat\":" + revokedIat + "}", null));

        String result = adapter.checkTokenRevocation(userId, sid, tokenIat);

        assertEquals("ALL_TOKENS_REVOKED", result);
    }

    @Test
    @DisplayName("Refresh token kontrolü: İptal kaydı yoksa null dönmeli")
    void shouldReturnNull_WhenNoRevocationFound() {
        String userId = UUID.randomUUID().toString();
        String sid = UUID.randomUUID().toString();

        when(valueOperations.multiGet(Arrays.asList("user:" + userId + ":revoke", "sid:" + sid + ":revoke")))
                .thenReturn(Arrays.asList(null, null));

        String result = adapter.checkTokenRevocation(userId, sid, 1000L);

        assertNull(result);
    }
}
