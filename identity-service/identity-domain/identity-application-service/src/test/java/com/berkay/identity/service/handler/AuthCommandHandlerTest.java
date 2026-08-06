package com.berkay.identity.service.handler;

import com.berkay.identity.service.dto.command.LoginCommand;
import com.berkay.identity.service.dto.command.RefreshTokenCommand;
import com.berkay.identity.service.dto.command.TokenResponse;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Base64;
import java.util.Optional;

import com.berkay.identity.service.domain.exception.TokenRevokedDomainException;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.AccountStatus;
import com.berkay.identity.service.domain.valueobject.UserEmail;
import com.berkay.identity.service.ports.output.repository.TokenRevocationPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AuthCommandHandlerTest {

    @Mock
    private IdentityProviderPort identityProviderPort;
    
    @Mock
    private TokenRevocationPort tokenRevocationPort;

    @Mock
    private com.berkay.identity.service.ports.output.repository.UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthCommandHandler authCommandHandler;

    private String createFakeJwt(String payloadJson) {
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes());
        return "header." + encodedPayload + ".signature";
    }

    @Test
    @DisplayName("Başarılı Senaryo: Geçerli bilgilerle login olunduğunda TokenResponse dönmelidir.")
    void shouldReturnTokenResponse_WhenLoginIsSuccessful() {
        // given
        LoginCommand command = LoginCommand.builder()
                .email("test@test.com")
                .password("Password123!")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(identityProviderPort.login("test@test.com", "Password123!")).thenReturn(tokenResponse);

        // when
        TokenResponse response = authCommandHandler.login(command);

        // then
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(identityProviderPort).login("test@test.com", "Password123!");
    }

    @Test
    @DisplayName("Hata Senaryosu: Banned olan bir user login olmaya çalışırsa IdentityDomainException fırlatılmalıdır.")
    void shouldThrowException_WhenUserIsBanned() {
        // given
        LoginCommand command = LoginCommand.builder()
                .email("banned@test.com")
                .password("Password123!")
                .build();

        User bannedUser = mock(User.class);
        when(bannedUser.getStatus()).thenReturn(AccountStatus.BANNED);
        when(userRepository.findByEmail("banned@test.com")).thenReturn(Optional.of(bannedUser));

        // when & then
        IdentityDomainException ex = assertThrows(IdentityDomainException.class, () -> authCommandHandler.login(command));
        assertEquals("Your account has been banned.", ex.getMessage());
        verify(identityProviderPort, never()).login(anyString(), anyString());
    }


    @Test
    @DisplayName("Başarılı Senaryo: Geçerli refresh token ile yeni token dönmelidir.")
    void shouldReturnNewToken_WhenRefreshTokenIsSuccessful() throws Exception {
        // given
        String fakeToken = createFakeJwt("{\"sid\":\"session-123\", \"internal_id\":\"user-123\", \"iat\":1000}");
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                .refreshToken(fakeToken)
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode sidNode = mock(JsonNode.class);
        JsonNode internalIdNode = mock(JsonNode.class);
        JsonNode subNode = mock(JsonNode.class);
        JsonNode iatNode = mock(JsonNode.class);

        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);
        when(mockJsonNode.path("sid")).thenReturn(sidNode);
        when(mockJsonNode.path("internal_id")).thenReturn(internalIdNode);
        lenient().when(mockJsonNode.path("sub")).thenReturn(subNode);
        when(mockJsonNode.path("iat")).thenReturn(iatNode);

        when(sidNode.asText(null)).thenReturn("session-123");
        when(internalIdNode.asText(null)).thenReturn("user-123");
        when(iatNode.asLong(0)).thenReturn(1000L);

        when(tokenRevocationPort.checkTokenRevocation("user-123", "session-123", 1000L)).thenReturn(null);
        when(identityProviderPort.refreshToken(fakeToken)).thenReturn(tokenResponse);

        // when
        TokenResponse response = authCommandHandler.refreshToken(command);

        // then
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(identityProviderPort).refreshToken(fakeToken);
        verify(tokenRevocationPort).checkTokenRevocation("user-123", "session-123", 1000L);
    }

    @Test
    @DisplayName("Hata Senaryosu: Single Logout (sid) olan refresh token exception fırlatmalıdır.")
    void shouldThrowException_WhenSingleLogoutDetected() throws Exception {
        // given
        String fakeToken = createFakeJwt("{\"sid\":\"session-123\", \"internal_id\":\"user-123\", \"iat\":1000}");
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                .refreshToken(fakeToken)
                .build();

        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode sidNode = mock(JsonNode.class);
        JsonNode internalIdNode = mock(JsonNode.class);
        JsonNode iatNode = mock(JsonNode.class);

        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);
        when(mockJsonNode.path("sid")).thenReturn(sidNode);
        when(mockJsonNode.path("internal_id")).thenReturn(internalIdNode);
        when(mockJsonNode.path("iat")).thenReturn(iatNode);

        when(sidNode.asText(null)).thenReturn("session-123");
        when(internalIdNode.asText(null)).thenReturn("user-123");
        when(iatNode.asLong(0)).thenReturn(1000L);

        when(tokenRevocationPort.checkTokenRevocation("user-123", "session-123", 1000L)).thenReturn("ALL_TOKENS_REVOKED");

        // when & then
        TokenRevokedDomainException ex = assertThrows(TokenRevokedDomainException.class, () -> authCommandHandler.refreshToken(command));
        assertEquals("ALL_TOKENS_REVOKED", ex.getErrorCode());
        verify(identityProviderPort, never()).refreshToken(anyString());
    }

    @Test
    @DisplayName("Başarılı Senaryo: Sadece ACCESS_TOKEN_REVOKED ise refresh token devam etmelidir.")
    void shouldNotThrowException_WhenOnlyAccessTokenRevoked() throws Exception {
        // given
        String fakeToken = createFakeJwt("{\"sid\":\"session-123\", \"internal_id\":\"user-123\", \"iat\":1000}");
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                .refreshToken(fakeToken)
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode sidNode = mock(JsonNode.class);
        JsonNode internalIdNode = mock(JsonNode.class);
        JsonNode iatNode = mock(JsonNode.class);

        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);
        when(mockJsonNode.path("sid")).thenReturn(sidNode);
        when(mockJsonNode.path("internal_id")).thenReturn(internalIdNode);
        when(mockJsonNode.path("iat")).thenReturn(iatNode);

        when(sidNode.asText(null)).thenReturn("session-123");
        when(internalIdNode.asText(null)).thenReturn("user-123");
        when(iatNode.asLong(0)).thenReturn(1000L);

        when(tokenRevocationPort.checkTokenRevocation("user-123", "session-123", 1000L)).thenReturn("ACCESS_TOKEN_REVOKED");
        when(identityProviderPort.refreshToken(fakeToken)).thenReturn(tokenResponse);

        // when
        TokenResponse response = authCommandHandler.refreshToken(command);

        // then
        assertEquals("new-access-token", response.getAccessToken());
        verify(identityProviderPort).refreshToken(fakeToken);
    }

    @Test
    @DisplayName("Ortalama Senaryo: internal_id olmadan çalışır ve session üzerinden blocklanır.")
    void shouldBlockEvenIfInternalIdIsMissing() throws Exception {
        // given
        String fakeToken = createFakeJwt("{\"sid\":\"session-123\", \"iat\":1000}");
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                .refreshToken(fakeToken)
                .build();

        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode sidNode = mock(JsonNode.class);
        JsonNode internalIdNode = mock(JsonNode.class);
        JsonNode subNode = mock(JsonNode.class);
        JsonNode iatNode = mock(JsonNode.class);

        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);
        when(mockJsonNode.path("sid")).thenReturn(sidNode);
        when(mockJsonNode.path("internal_id")).thenReturn(internalIdNode);
        when(mockJsonNode.path("sub")).thenReturn(subNode);
        when(mockJsonNode.path("iat")).thenReturn(iatNode);

        when(sidNode.asText(null)).thenReturn("session-123");
        when(internalIdNode.asText(null)).thenReturn(null); // Missing internal_id
        when(subNode.asText(null)).thenReturn("sub-123"); // Has sub

        User mockUser = mock(User.class);
        UserId mockUserId = mock(UserId.class);
        when(mockUserId.getValue()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        when(mockUser.getId()).thenReturn(mockUserId);
        when(userRepository.findByExternalId("sub-123")).thenReturn(Optional.of(mockUser));

        when(iatNode.asLong(0)).thenReturn(1000L);

        when(tokenRevocationPort.checkTokenRevocation("11111111-1111-1111-1111-111111111111", "session-123", 1000L)).thenReturn("ALL_TOKENS_REVOKED");

        // when & then
        TokenRevokedDomainException ex = assertThrows(TokenRevokedDomainException.class, () -> authCommandHandler.refreshToken(command));
        assertEquals("ALL_TOKENS_REVOKED", ex.getErrorCode());
        verify(tokenRevocationPort).checkTokenRevocation("11111111-1111-1111-1111-111111111111", "session-123", 1000L);
    }
    @Test
    @DisplayName("Ortalama Senaryo: sid olmadan çalışır ve internal_id üzerinden blocklanır.")
    void shouldBlockWhenSidIsMissingAndAllTokensRevoked() throws Exception {
        // given
        String fakeToken = createFakeJwt("{\"internal_id\":\"user-123\", \"iat\":1000}");
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                .refreshToken(fakeToken)
                .build();

        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode sidNode = mock(JsonNode.class);
        JsonNode internalIdNode = mock(JsonNode.class);
        JsonNode iatNode = mock(JsonNode.class);

        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);
        when(mockJsonNode.path("sid")).thenReturn(sidNode);
        when(mockJsonNode.path("internal_id")).thenReturn(internalIdNode);
        when(mockJsonNode.path("iat")).thenReturn(iatNode);

        when(sidNode.asText(null)).thenReturn(null); // Missing sid
        when(internalIdNode.asText(null)).thenReturn("user-123");
        when(iatNode.asLong(0)).thenReturn(1000L);

        when(tokenRevocationPort.checkTokenRevocation("user-123", null, 1000L)).thenReturn("ALL_TOKENS_REVOKED");

        // when & then
        TokenRevokedDomainException ex = assertThrows(TokenRevokedDomainException.class, () -> authCommandHandler.refreshToken(command));
        assertEquals("ALL_TOKENS_REVOKED", ex.getErrorCode());
        verify(tokenRevocationPort).checkTokenRevocation("user-123", null, 1000L);
    }
}
