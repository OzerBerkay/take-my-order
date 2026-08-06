package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.query.ValidateUserQuery;
import com.berkay.identity.service.dto.query.ValidateUserResponse;
import com.berkay.identity.service.ports.input.service.UserApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/internal/users", produces = "application/json")
public class UserValidationController {

    private final UserApplicationService userApplicationService;

    @GetMapping("/validate")
    public ResponseEntity<ValidateUserResponse> validateUserForPersonnel(@RequestParam("email") String email) {
        log.info("Received internal request to validate user with email: {}", email);
        
        ValidateUserQuery query = ValidateUserQuery.builder()
                .email(email)
                .build();
                
        ValidateUserResponse response = userApplicationService.validateUserForPersonnel(query);
        
        return ResponseEntity.ok(response);
    }
}
