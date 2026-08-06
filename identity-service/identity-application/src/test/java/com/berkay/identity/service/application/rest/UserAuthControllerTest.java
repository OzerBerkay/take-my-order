package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.application.security.config.SecurityConfig;
import com.berkay.identity.service.dto.command.LoginCommand;
import com.berkay.identity.service.dto.command.RefreshTokenCommand;
import com.berkay.identity.service.dto.command.TokenResponse;
import com.berkay.identity.service.ports.input.service.AuthApplicationService;
import com.berkay.identity.service.ports.input.service.UserApplicationService;
import com.berkay.identity.service.dto.command.UpdatePasswordCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserAuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@ContextConfiguration(classes = {UserAuthController.class})
class UserAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserApplicationService userApplicationService;

    @MockBean
    private AuthApplicationService authApplicationService;

    @Test
    @DisplayName("Başarılı Senaryo: Login işlemi başarılı olunca HTTP 200 dönmelidir.")
    void shouldReturn200_WhenLoginIsSuccessful() throws Exception {
        LoginCommand command = LoginCommand.builder()
                .email("customer@takemyorder.com")
                .password("Customer123!")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .build();

        when(authApplicationService.login(any(LoginCommand.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"));
    }

    @Test
    @DisplayName("Alternatif Senaryo: Eksik bilgi ile login istendiğinde HTTP 400 dönmelidir.")
    void shouldReturn400_WhenLoginCommandIsInvalid() throws Exception {
        LoginCommand command = LoginCommand.builder()
                .email("customer@takemyorder.com")
                // Şifre boş bırakıldı
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Başarılı Senaryo: Refresh Token işlemi başarılı olunca HTTP 200 dönmelidir.")
    void shouldReturn200_WhenRefreshTokenIsSuccessful() throws Exception {
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                .refreshToken("valid-refresh-token")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(authApplicationService.refreshToken(any(RefreshTokenCommand.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("Alternatif Senaryo: Eksik bilgi ile refresh token istendiğinde HTTP 400 dönmelidir.")
    void shouldReturn400_WhenRefreshTokenCommandIsInvalid() throws Exception {
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                // refreshToken boş bırakıldı
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Başarılı Senaryo: Update Password işlemi başarılı olunca HTTP 204 dönmelidir.")
    void shouldReturn204_WhenUpdatePasswordIsSuccessful() throws Exception {
        UpdatePasswordCommand command = UpdatePasswordCommand.builder()
                .newPassword("Customer1234!")
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Alternatif Senaryo: Eksik bilgi ile şifre güncelleme istendiğinde HTTP 400 dönmelidir.")
    void shouldReturn400_WhenUpdatePasswordCommandIsInvalid() throws Exception {
        UpdatePasswordCommand command = UpdatePasswordCommand.builder()
                // newPassword boş bırakıldı
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }
}
