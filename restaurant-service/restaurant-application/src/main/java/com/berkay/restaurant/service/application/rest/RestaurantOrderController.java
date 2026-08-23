package com.berkay.restaurant.service.application.rest;

import com.berkay.restaurant.service.application.security.RestaurantAuthService;
import com.berkay.restaurant.service.domain.dto.approve.ApproveRestaurantOrderCommand;

import com.berkay.restaurant.service.domain.dto.reject.RejectRestaurantOrderCommand;
import com.berkay.restaurant.service.domain.ports.input.service.RestaurantOrderApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/restaurants/{restaurantId}/orders", produces = "application/vnd.api.v1+json")
public class RestaurantOrderController {

    private final RestaurantOrderApplicationService restaurantOrderApplicationService;
    private final RestaurantAuthService restaurantAuthService;



    @PostMapping("/{orderId}/approve")
    public ResponseEntity<Void> approveOrder(@PathVariable("restaurantId") UUID restaurantId,
                                             @PathVariable("orderId") UUID orderId) {
        log.info("Received request to approve order {} for restaurant: {}", orderId, restaurantId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean hasRestaurantSpecificPermission = restaurantAuthService.hasPermissionForRestaurant(authentication, "can_review_orders", restaurantId);
        boolean hasSystemPermission = restaurantAuthService.hasPermission(authentication, "can_review_orders");
        
        if (!hasRestaurantSpecificPermission && !hasSystemPermission) {
            log.warn("User does not have can_review_orders permission for restaurant {}", restaurantId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ApproveRestaurantOrderCommand command = ApproveRestaurantOrderCommand.builder()
                .restaurantId(restaurantId)
                .orderId(orderId)
                .build();

        restaurantOrderApplicationService.approveOrder(command);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/reject")
    public ResponseEntity<Void> rejectOrder(@PathVariable("restaurantId") UUID restaurantId,
                                            @PathVariable("orderId") UUID orderId,
                                            @RequestBody RejectRestaurantOrderCommand request) {
        log.info("Received request to reject order {} for restaurant: {}", orderId, restaurantId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean hasRestaurantSpecificPermission = restaurantAuthService.hasPermissionForRestaurant(authentication, "can_review_orders", restaurantId);
        boolean hasSystemPermission = restaurantAuthService.hasPermission(authentication, "can_review_orders");
        
        if (!hasRestaurantSpecificPermission && !hasSystemPermission) {
            log.warn("User does not have can_review_orders permission for restaurant {}", restaurantId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        RejectRestaurantOrderCommand command = RejectRestaurantOrderCommand.builder()
                .restaurantId(restaurantId)
                .orderId(orderId)
                .failureMessages(request.getFailureMessages())
                .build();

        restaurantOrderApplicationService.rejectOrder(command);

        return ResponseEntity.ok().build();
    }
}
