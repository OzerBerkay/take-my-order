package com.berkay.restaurant.service.application.rest;

import com.berkay.restaurant.service.application.security.RestaurantAuthService;
import com.berkay.restaurant.service.domain.AddPersonnelCommandHandler;
import com.berkay.restaurant.service.domain.dto.create.AddPersonnelCommand;
import com.berkay.restaurant.service.domain.dto.create.AddPersonnelResponse;
import com.berkay.restaurant.service.domain.RemovePersonnelCommandHandler;
import com.berkay.restaurant.service.domain.dto.delete.RemovePersonnelCommand;
import com.berkay.restaurant.service.domain.dto.delete.RemovePersonnelResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/restaurants", produces = "application/vnd.api.v1+json")
public class RestaurantPersonnelController {

    private final AddPersonnelCommandHandler addPersonnelCommandHandler;
    private final RemovePersonnelCommandHandler removePersonnelCommandHandler;
    private final RestaurantAuthService restaurantAuthService;

    @PostMapping("/{restaurantId}/personnel")
    public ResponseEntity<AddPersonnelResponse> addPersonnel(@PathVariable("restaurantId") UUID restaurantId,
                                                             @RequestBody Map<String, String> request) {
        log.info("Received request to add personnel to restaurant: {}", restaurantId);

        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Use RestaurantAuthService to check permission specifically for this restaurantId
        if (!restaurantAuthService.hasPermissionForRestaurant(authentication, "can_add_personnel", restaurantId)) {
            log.warn("User does not have can_add_personnel permission for restaurant {}", restaurantId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UUID merchantId = extractUserIdFromAuthentication(authentication);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AddPersonnelCommand command = AddPersonnelCommand.builder()
                .restaurantId(restaurantId)
                .addedByMerchantId(merchantId)
                .email(email)
                .build();

        AddPersonnelResponse response = addPersonnelCommandHandler.addPersonnel(command);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{restaurantId}/personnel/{userId}")
    public ResponseEntity<RemovePersonnelResponse> removePersonnel(@PathVariable("restaurantId") UUID restaurantId,
                                                                   @PathVariable("userId") UUID userId) {
        log.info("Received request to remove personnel {} from restaurant: {}", userId, restaurantId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!restaurantAuthService.hasPermissionForRestaurant(authentication, "can_remove_personnel", restaurantId)) {
            log.warn("User does not have can_remove_personnel permission for restaurant {}", restaurantId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UUID merchantId = extractUserIdFromAuthentication(authentication);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RemovePersonnelCommand command = RemovePersonnelCommand.builder()
                .restaurantId(restaurantId)
                .userId(userId)
                .removedByMerchantId(merchantId)
                .build();

        RemovePersonnelResponse response = removePersonnelCommandHandler.removePersonnel(command);

        return ResponseEntity.ok(response);
    }

    private UUID extractUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication instanceof com.berkay.application.security.JwtAuthenticationToken) {
            return ((com.berkay.application.security.JwtAuthenticationToken) authentication).getInternalId();
        }
        return null;
    }
}
