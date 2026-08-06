package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.query.ValidateUserQuery;
import com.berkay.identity.service.dto.query.ValidateUserResponse;
import com.berkay.identity.service.ports.input.service.UserApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidationControllerTest {

    @Mock
    private UserApplicationService userApplicationService;

    @InjectMocks
    private UserValidationController userValidationController;

    private String email;
    private UUID userId;

    @BeforeEach
    void setUp() {
        email = "test@test.com";
        userId = UUID.randomUUID();
    }

    @Test
    void shouldReturnValidationResponse() {
        ValidateUserResponse mockResponse = ValidateUserResponse.builder()
                .userId(userId)
                .valid(true)
                .build();

        when(userApplicationService.validateUserForPersonnel(any(ValidateUserQuery.class)))
                .thenReturn(mockResponse);

        ResponseEntity<ValidateUserResponse> responseEntity = userValidationController.validateUserForPersonnel(email);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());
        assertEquals(userId, responseEntity.getBody().getUserId());
        assertEquals(true, responseEntity.getBody().isValid());
    }
}
