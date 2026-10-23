package com.berkay.order.service.application.rest;

import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrdersPageResponse;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrdersQuery;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrdersSummaryResponse;
import com.berkay.order.service.domain.ports.input.service.OrderApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/orders/restaurants", produces = MediaType.APPLICATION_JSON_VALUE)
public class RestaurantOrderQueryController {

    private final OrderApplicationService orderApplicationService;

    @PreAuthorize("@orderAuthService.hasPermissionForRestaurant(authentication, 'can_read_orders', #restaurantId) or @orderAuthService.hasPermission(authentication, 'can_read_all_orders')")
    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantOrdersPageResponse> getRestaurantOrders(
            @PathVariable("restaurantId") UUID restaurantId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        log.info("Received request to get orders for restaurant: {}, status: {}, search: {}, page: {}, size: {}, sort: {}",
                restaurantId, status, search, page, size, sort);

        RestaurantOrdersQuery query = RestaurantOrdersQuery.builder()
                .restaurantId(restaurantId)
                .status(status)
                .search(search)
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        RestaurantOrdersPageResponse response = orderApplicationService.getRestaurantOrders(query);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermissionForRestaurant(authentication, 'can_read_orders', #restaurantId) or @orderAuthService.hasPermission(authentication, 'can_read_all_orders')")
    @GetMapping("/{restaurantId}/summary")
    public ResponseEntity<RestaurantOrdersSummaryResponse> getRestaurantOrdersSummary(
            @PathVariable("restaurantId") UUID restaurantId) {
        log.info("Received request to get orders summary for restaurant: {}", restaurantId);
        RestaurantOrdersSummaryResponse response = orderApplicationService.getRestaurantOrdersSummary(restaurantId);
        return ResponseEntity.ok(response);
    }
}
