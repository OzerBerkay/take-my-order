package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.command.UpdateUserProfileCommand;
import com.berkay.identity.service.dto.command.UpdateUserProfileResponse;
import com.berkay.identity.service.ports.input.service.UserApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/users/me", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserApplicationService userApplicationService;

    @PatchMapping("/profile")
    public ResponseEntity<UpdateUserProfileResponse> updateUserProfile(@RequestBody @Valid UpdateUserProfileCommand command) {
        log.info("Received PATCH request to update user profile");
        UpdateUserProfileResponse response = userApplicationService.updateUserProfile(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<com.berkay.identity.service.dto.query.UserResponse> getMyProfile() {
        java.util.UUID userId = getAuthenticatedUserId();
        log.info("Received GET request for my profile: {}", userId);
        return ResponseEntity.ok(userApplicationService.getUserProfile(userId));
    }



    @GetMapping("/organizational-units")
    public ResponseEntity<java.util.List<java.util.UUID>> getMyOrganizationalUnits() {
        java.util.UUID userId = getAuthenticatedUserId();
        log.info("Received GET request for my organizational units: {}", userId);
        com.berkay.identity.service.dto.query.UserResponse profile = userApplicationService.getUserProfile(userId);
        return ResponseEntity.ok(profile.getOrganizationalUnitIds());
    }

    private java.util.UUID getAuthenticatedUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getInternalId();
        }
        throw new com.berkay.identity.service.domain.exception.IdentityDomainException("Could not extract user id from security context");
    }

}
