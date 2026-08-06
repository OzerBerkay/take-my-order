package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.command.UpdateUserProfileCommand;
import com.berkay.identity.service.dto.command.UpdateUserProfileResponse;
import com.berkay.identity.service.ports.input.service.UserApplicationService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserProfileController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.berkay.identity.service.application.security.config.SecurityConfig.class))
@ContextConfiguration(classes = {UserProfileController.class})
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserApplicationService userApplicationService;

    @Test
    @DisplayName("Başarılı Senaryo: Profil güncellenince HTTP 200 dönmelidir.")
    void shouldReturn200_WhenUpdateProfileIsSuccessful() throws Exception {
        UpdateUserProfileCommand command = UpdateUserProfileCommand.builder()
                .firstName("Updated First")
                .lastName("Updated Last")
                .imageUrl("https://example.com/updated.jpg")
                .build();

        UpdateUserProfileResponse response = UpdateUserProfileResponse.builder()
                .message("Profile successfully updated")
                .build();

        when(userApplicationService.updateUserProfile(any(UpdateUserProfileCommand.class))).thenReturn(response);

        mockMvc.perform(patch("/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile successfully updated"));
    }

    @Test
    @DisplayName("Alternatif Senaryo: Eksik bilgi ile profil güncellenmeye çalışıldığında HTTP 400 dönmelidir.")
    void shouldReturn400_WhenUpdateProfileCommandIsInvalid() throws Exception {
        UpdateUserProfileCommand command = UpdateUserProfileCommand.builder()
                // firstName and lastName must be present according to validation (assuming @NotBlank is on them)
                .build();

        mockMvc.perform(patch("/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }
}
