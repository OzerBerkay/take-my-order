package com.berkay.order.service.application.rest;

import com.berkay.order.service.domain.dto.create.CreateOrderCommand;
import com.berkay.order.service.domain.dto.create.CreateOrderResponse;
import com.berkay.order.service.domain.dto.operation.*;
import com.berkay.order.service.domain.dto.read.CustomerOrderDetailQuery;
import com.berkay.order.service.domain.dto.read.CustomerOrderDetailResponse;
import com.berkay.order.service.domain.dto.read.GetOrdersQuery;
import com.berkay.order.service.domain.dto.read.GetOrdersResponse;
import com.berkay.order.service.domain.dto.read.OrderFilterGroup;
import com.berkay.order.service.domain.dto.restaurant.OrderDetailQuery;
import com.berkay.order.service.domain.dto.restaurant.PlatformOrderQuery;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.dto.track.TrackOrderQuery;
import com.berkay.order.service.domain.dto.track.TrackOrderResponse;
import com.berkay.order.service.domain.ports.input.service.OrderApplicationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PreAuthorize("@orderAuthService.isCustomer(authentication)")
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @AuthenticationPrincipal UUID customerId,
            @RequestBody @Valid CreateOrderCommand createOrderCommand) {

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

    @PreAuthorize("@orderAuthService.isCustomer(authentication)")
    @PostAuthorize("@orderAuthService.isOwner(authentication, returnObject.body.customerId)")
    @GetMapping("/{trackingId}/track")
    public ResponseEntity<TrackOrderResponse> getOrderByTrackingId(@PathVariable UUID trackingId) {
        TrackOrderResponse trackOrderResponse =
                orderApplicationService.trackOrder(TrackOrderQuery.builder().orderTrackingId(trackingId).build());
        log.info("Returning order status with tracking id: {}", trackOrderResponse.getOrderTrackingId());
        return ResponseEntity.ok(trackOrderResponse);
    }

    @PreAuthorize("@orderAuthService.isOrderCustomer(authentication, #orderId) or @orderAuthService.hasPermission(authentication, 'can_read_all_orders')")
    @GetMapping("/{orderId}")
    public ResponseEntity<CustomerOrderDetailResponse> getCustomerOrderDetail(@PathVariable UUID orderId) {
        log.info("Getting customer order detail for order with id: {}", orderId);
        CustomerOrderDetailResponse response = orderApplicationService.getCustomerOrderDetail(
                CustomerOrderDetailQuery.builder().orderId(orderId).build());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.isCustomer(authentication)")
    @GetMapping
    public ResponseEntity<GetOrdersResponse> getOrders(
            @AuthenticationPrincipal UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ALL") OrderFilterGroup filter) {

        log.info("Getting orders for customer: {} with filter: {}", customerId, filter);

        GetOrdersResponse response = orderApplicationService.getOrders(
                GetOrdersQuery.builder()
                        .customerId(customerId)
                        .page(page)
                        .size(size)
                        .filter(filter)
                        .build()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermissionForOrder(authentication, 'can_approve_order', #orderId)")
    @PostMapping("/{orderId}/approve")
    public ResponseEntity<RestaurantOrderResponse> approveOrder(@PathVariable UUID orderId) {
        log.info("Approving order with id: {}", orderId);
        RestaurantOrderResponse response = orderApplicationService.approveOrder(
                ApproveOrderCommand.builder().orderId(orderId).build());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermissionForOrder(authentication, 'can_start_preparing_order', #orderId)")
    @PostMapping("/{orderId}/start-preparing")
    public ResponseEntity<RestaurantOrderResponse> startPreparingOrder(@PathVariable UUID orderId) {
        log.info("Starting preparation for order with id: {}", orderId);
        RestaurantOrderResponse response = orderApplicationService.startPreparingOrder(
                StartPreparingOrderCommand.builder().orderId(orderId).build());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermissionForOrder(authentication, 'can_ready_order', #orderId)")
    @PostMapping("/{orderId}/ready-for-pickup")
    public ResponseEntity<RestaurantOrderResponse> readyForPickupOrder(@PathVariable UUID orderId) {
        log.info("Order with id: {} is ready for pickup", orderId);
        RestaurantOrderResponse response = orderApplicationService.readyForPickupOrder(
                ReadyForPickupOrderCommand.builder().orderId(orderId).build());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermissionForOrder(authentication, 'can_start_delivery', #orderId)")
    @PostMapping("/{orderId}/start-delivery")
    public ResponseEntity<RestaurantOrderResponse> startDeliveryOrder(@PathVariable UUID orderId) {
        log.info("Starting delivery for order with id: {}", orderId);
        RestaurantOrderResponse response = orderApplicationService.startDeliveryOrder(
                StartDeliveryOrderCommand.builder().orderId(orderId).build());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermissionForOrder(authentication, 'can_deliver_order', #orderId)")
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<RestaurantOrderResponse> deliverOrder(@PathVariable UUID orderId) {
        log.info("Delivering order with id: {}", orderId);
        RestaurantOrderResponse response = orderApplicationService.deliverOrder(
                DeliverOrderCommand.builder().orderId(orderId).build());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasCancelPermissionForOrder(authentication, #orderId)")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<RestaurantOrderResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody @Valid CancelOrderCommand command) {
        log.info("Cancelling order with id: {}, reason: {}", orderId, command.getCancelReason());
        command.setOrderId(orderId);
        RestaurantOrderResponse response = orderApplicationService.cancelOrder(command);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermission(authentication, 'can_force_cancel_order')")
    @PostMapping("/{orderId}/force-cancel")
    public ResponseEntity<RestaurantOrderResponse> forceCancelOrder(
            @PathVariable UUID orderId,
            @RequestBody @Valid ForceCancelOrderCommand command) {
        log.info("Force cancelling order with id: {} by admin, forceRefund: {}", orderId, command.isForceRefund());
        command.setOrderId(orderId);
        RestaurantOrderResponse response = orderApplicationService.forceCancelOrder(command);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermissionForOrder(authentication, 'can_read_orders', #orderId)")
    @GetMapping("/{orderId}/detail")
    public ResponseEntity<RestaurantOrderResponse> getOrderDetail(@PathVariable UUID orderId) {
        log.info("Getting order detail for order with id: {}", orderId);
        RestaurantOrderResponse response = orderApplicationService.getOrderDetail(
                OrderDetailQuery.builder().orderId(orderId).build());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermission(authentication, 'can_read_all_orders')")
    @GetMapping("/admin/{trackingId}")
    public ResponseEntity<RestaurantOrderResponse> getPlatformOrder(@PathVariable UUID trackingId) {
        log.info("Getting platform order by tracking id: {}", trackingId);
        RestaurantOrderResponse response = orderApplicationService.getPlatformOrder(
                PlatformOrderQuery.builder().trackingId(trackingId).build());
        return ResponseEntity.ok(response);
    }
}
