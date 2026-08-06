package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.dto.command.RegisterCustomerCommand;
import com.berkay.identity.service.dto.command.RegisterMerchantCommand;
import com.berkay.identity.service.dto.command.LoginCommand;
import com.berkay.identity.service.dto.command.RefreshTokenCommand;
import com.berkay.identity.service.dto.command.TokenResponse;
import com.berkay.identity.service.dto.command.UpdatePasswordCommand;
import com.berkay.identity.service.ports.input.service.AuthApplicationService;
import com.berkay.identity.service.ports.input.service.UserApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/auth", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class UserAuthController {
    private final UserApplicationService userApplicationService;
    private final AuthApplicationService authApplicationService;

    @PostMapping("/register/customer")
    public ResponseEntity<CreateUserResponse> registerCustomer(@RequestBody @Valid RegisterCustomerCommand command) {
        log.info("Received register customer request for email: {}", command.getEmail());
        CreateUserResponse response = userApplicationService.registerCustomer(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/merchant")
    public ResponseEntity<CreateUserResponse> registerMerchant(@RequestBody @Valid RegisterMerchantCommand command) {
        log.info("Received register merchant request for email: {}", command.getEmail());
        CreateUserResponse response = userApplicationService.registerMerchant(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginCommand command) {
        log.info("Received login request for user: {}", command.getEmail());
        TokenResponse response = authApplicationService.login(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody @Valid RefreshTokenCommand command) {
        log.info("Received refresh token request");
        TokenResponse response = authApplicationService.refreshToken(command);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody @Valid UpdatePasswordCommand command) {
        log.info("Received password update request for current user");
        authApplicationService.updatePassword(command);
        return ResponseEntity.noContent().build();
    }
}