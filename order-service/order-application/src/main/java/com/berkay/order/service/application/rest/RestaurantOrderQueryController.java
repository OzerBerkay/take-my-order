package com.berkay.order.service.application.rest;

import com.berkay.order.service.domain.dto.query.PaidOrderResponse;
import com.berkay.order.service.domain.ports.input.service.OrderApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/orders/restaurants", produces = "application/vnd.api.v1+json")
public class RestaurantOrderQueryController {

    private final OrderApplicationService orderApplicationService;

    @org.springframework.security.access.prepost.PreAuthorize("@orderAuthService.hasPermissionForRestaurant(authentication, 'can_read_orders', #restaurantId) or @orderAuthService.hasPermission(authentication, 'can_read_orders')")
    @GetMapping("/{restaurantId}/paid")
    public ResponseEntity<List<PaidOrderResponse>> getPaidOrders(@PathVariable("restaurantId") UUID restaurantId) {
        log.info("Received request to get paid orders for restaurant: {}", restaurantId);

        List<PaidOrderResponse> response = orderApplicationService.getPaidOrders(restaurantId);
        return ResponseEntity.ok(response);
    }
}
