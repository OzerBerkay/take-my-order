package com.berkay.order.service.application.rest;

import com.berkay.order.service.domain.dto.create.CreateOrderCommand;
import com.berkay.order.service.domain.dto.create.CreateOrderResponse;
import com.berkay.order.service.domain.dto.track.TrackOrderQuery;
import com.berkay.order.service.domain.dto.track.TrackOrderResponse;
import com.berkay.order.service.domain.ports.input.service.OrderApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/orders", produces = "application/vnd.api.v1+json")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @org.springframework.security.access.prepost.PreAuthorize("@orderAuthService.isCustomer(authentication)")
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UUID customerId,
            @RequestBody @jakarta.validation.Valid CreateOrderCommand createOrderCommand) {
        
        CreateOrderCommand commandWithCustomerId = CreateOrderCommand.builder()
                .customerId(customerId)
                .restaurantId(createOrderCommand.getRestaurantId())
                .price(createOrderCommand.getPrice())
                .deliveryFee(createOrderCommand.getDeliveryFee())
                .items(createOrderCommand.getItems())
                .address(createOrderCommand.getAddress())
                .build();
                
        log.info("Creating order for customer: {} at restaurant: {}", customerId,
                commandWithCustomerId.getRestaurantId());
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(commandWithCustomerId);
        log.info("Order created with tracking id: {}", createOrderResponse.getOrderTrackingId());
        return ResponseEntity.ok(createOrderResponse);
    }

    @org.springframework.security.access.prepost.PreAuthorize("@orderAuthService.isCustomer(authentication)")
    @org.springframework.security.access.prepost.PostAuthorize("@orderAuthService.isOwner(authentication, returnObject.body.customerId)")
    @GetMapping("/{trackingId}")
    public ResponseEntity<TrackOrderResponse> getOrderByTrackingId(@PathVariable UUID trackingId) {
        TrackOrderResponse trackOrderResponse =
                orderApplicationService.trackOrder(TrackOrderQuery.builder().orderTrackingId(trackingId).build());
        log.info("Returning order status with tracking id: {}", trackOrderResponse.getOrderTrackingId());
        return  ResponseEntity.ok(trackOrderResponse);
    }

    @org.springframework.security.access.prepost.PreAuthorize("@orderAuthService.isCustomer(authentication)")
    @GetMapping
    public ResponseEntity<com.berkay.order.service.domain.dto.read.GetOrdersResponse> getOrders(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        log.info("Getting orders for customer: {}", customerId);
        
        com.berkay.order.service.domain.dto.read.GetOrdersResponse response = orderApplicationService.getOrders(
                com.berkay.order.service.domain.dto.read.GetOrdersQuery.builder()
                        .customerId(customerId)
                        .page(page)
                        .size(size)
                        .build()
        );
        return ResponseEntity.ok(response);
    }
}
