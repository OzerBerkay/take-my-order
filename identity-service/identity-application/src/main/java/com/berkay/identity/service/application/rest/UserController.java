package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.command.TokenRevocationResponse;
import com.berkay.identity.service.handler.UserLogoutCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/users", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class UserController {

    private final UserLogoutCommandHandler userLogoutCommandHandler;

    @PostMapping("/logout")
    public ResponseEntity<TokenRevocationResponse> logout() {
        log.info("Received POST request for single logout");
        return ResponseEntity.ok(userLogoutCommandHandler.logout());
    }

    @PostMapping("/logout-all")
    public ResponseEntity<TokenRevocationResponse> logoutAll() {
        log.info("Received POST request for global logout");
        return ResponseEntity.ok(userLogoutCommandHandler.logoutAll());
    }
}
