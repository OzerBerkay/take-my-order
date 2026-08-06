package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.command.AddAddressCommand;
import com.berkay.identity.service.dto.command.AddressResponse;
import com.berkay.identity.service.dto.command.UpdateAddressCommand;
import com.berkay.identity.service.dto.query.UserAddressResponse;
import com.berkay.identity.service.ports.input.service.AddressApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/users/me/addresses", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class UserAddressController {

    private final AddressApplicationService addressApplicationService;

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@RequestBody @Valid AddAddressCommand command) {
        log.info("Received request to add a new address");
        AddressResponse response = addressApplicationService.addAddress(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable UUID addressId,
                                                         @RequestBody @Valid UpdateAddressCommand command) {
        log.info("Received request to update address id: {}", addressId);
        // Ensure path variable matches command if command has addressId
        if (!addressId.equals(command.getAddressId())) {
            return ResponseEntity.badRequest().build();
        }
        AddressResponse response = addressApplicationService.updateAddress(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<java.util.List<UserAddressResponse>> getMyAddresses() {
        UUID userId = getAuthenticatedUserId();
        log.info("Received request to get all addresses for user: {}", userId);
        return ResponseEntity.ok(addressApplicationService.getMyAddresses(userId));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> getMyAddressById(@PathVariable UUID addressId) {
        UUID userId = getAuthenticatedUserId();
        log.info("Received request to get address {} for user: {}", addressId, userId);
        return ResponseEntity.ok(addressApplicationService.getMyAddressById(userId, addressId));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteMyAddress(@PathVariable UUID addressId) {
        UUID userId = getAuthenticatedUserId();
        log.info("Received request to delete address {} for user: {}", addressId, userId);
        addressApplicationService.deleteMyAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    private UUID getAuthenticatedUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getInternalId();
        }
        throw new com.berkay.identity.service.domain.exception.IdentityDomainException("Could not extract user id from security context");
    }
}
