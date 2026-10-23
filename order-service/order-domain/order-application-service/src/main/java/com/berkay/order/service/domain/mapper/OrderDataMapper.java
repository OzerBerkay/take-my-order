package com.berkay.order.service.domain.mapper;

import com.berkay.domain.valueobject.*;
import com.berkay.order.service.domain.dto.create.CreateOrderCommand;
import com.berkay.order.service.domain.dto.create.CreateOrderResponse;
import com.berkay.order.service.domain.dto.create.OrderAddress;
import com.berkay.order.service.domain.dto.message.CustomerModel;
import com.berkay.order.service.domain.dto.track.TrackOrderResponse;
import com.berkay.order.service.domain.entity.*;
import com.berkay.order.service.domain.event.OrderCancelledEvent;
import com.berkay.order.service.domain.event.OrderCreatedEvent;
import com.berkay.order.service.domain.event.OrderPaidEvent;
import com.berkay.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.berkay.order.service.domain.outbox.model.approval.OrderApprovalEventProduct;
import com.berkay.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.berkay.order.service.domain.valueobject.StreetAddress;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component // we can inject and use this from service classes
public class OrderDataMapper {

    public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
        return Order.builder()
                .customerId(new CustomerId(createOrderCommand.getCustomerId()))
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .deliveryAddress(orderAddressToStreetAddress(createOrderCommand.getAddress()))
                .price(new Money(createOrderCommand.getPrice()))
                .deliveryFee(new Money(createOrderCommand.getDeliveryFee()))
                .items(orderItemsToOrderItemEntities(createOrderCommand.getItems()))
                .build();
    }

    public CreateOrderResponse orderToCreateOrderResponse(Order order, String message) {
        return CreateOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .orderStatus(order.getOrderStatus())
                .message(message)
                .build();
    }

    public TrackOrderResponse orderToTrackOrderResponse(Order order) {
        List<String> failureMessages = order.getCancelReason() != null
                ? List.of(order.getCancelReason().name())
                : (order.getFailureMessages() != null ? order.getFailureMessages() : List.of());
        return TrackOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .customerId(order.getCustomerId().getValue())
                .orderStatus(order.getOrderStatus())
                .failureMessages(failureMessages)
                .cancelReason(order.getCancelReason())
                .internalNote(order.getInternalNote())
                .build();
    }

    public OrderPaymentEventPayload orderCreatedEventToOrderPaymentEventPayload(OrderCreatedEvent orderCreatedEvent) {
        return OrderPaymentEventPayload.builder()
                .customerId(orderCreatedEvent.getOrder().getCustomerId().getValue().toString())
                .restaurantId(orderCreatedEvent.getOrder().getRestaurantId().getValue().toString())
                .orderId(orderCreatedEvent.getOrder().getId().getValue().toString())
                .price(orderCreatedEvent.getOrder().getPrice().getAmount())
                .createdAt(orderCreatedEvent.getCreatedAt())
                .paymentOrderStatus(PaymentOrderStatus.PENDING.name())
                .build();
    }

    public OrderPaymentEventPayload orderCancelledEventToOrderPaymentEventPayload(OrderCancelledEvent
                                                                                          orderCancelledEvent) {
        return OrderPaymentEventPayload.builder()
                .customerId(orderCancelledEvent.getOrder().getCustomerId().getValue().toString())
                .restaurantId(orderCancelledEvent.getOrder().getRestaurantId().getValue().toString())
                .orderId(orderCancelledEvent.getOrder().getId().getValue().toString())
                .price(orderCancelledEvent.getOrder().getPrice().getAmount())
                .createdAt(orderCancelledEvent.getCreatedAt())
                .paymentOrderStatus(PaymentOrderStatus.CANCELLED.name())
                .build();
    }

    public OrderApprovalEventPayload orderPaidEventToOrderApprovalEventPayload(OrderPaidEvent orderPaidEvent) {
        return OrderApprovalEventPayload.builder()
                .orderId(orderPaidEvent.getOrder().getId().getValue().toString())
                .restaurantId(orderPaidEvent.getOrder().getRestaurantId().getValue().toString())
                .restaurantOrderStatus(RestaurantOrderStatus.PAID.name())
                .products(orderPaidEvent.getOrder().getItems().stream().map(orderItem ->
                        OrderApprovalEventProduct.builder()
                                .id(orderItem.getProduct().getId().getValue().toString())
                                .quantity(orderItem.getQuantity())
                                .build()).collect(Collectors.toList()))
                .price(orderPaidEvent.getOrder().getPrice().getAmount())
                .createdAt(orderPaidEvent.getCreatedAt())
                .build();
    }

    public OrderPaymentEventPayload orderToOrderPaymentEventPayload(Order order, PaymentOrderStatus paymentOrderStatus) {
        return OrderPaymentEventPayload.builder()
                .customerId(order.getCustomerId().getValue().toString())
                .restaurantId(order.getRestaurantId().getValue().toString())
                .orderId(order.getId().getValue().toString())
                .price(order.getPrice().getAmount())
                .createdAt(java.time.ZonedDateTime.now(java.time.ZoneId.of(com.berkay.domain.DomainConstants.UTC)))
                .paymentOrderStatus(paymentOrderStatus.name())
                .build();
    }

    public OrderApprovalEventPayload orderToOrderApprovalEventPayload(Order order, RestaurantOrderStatus restaurantOrderStatus) {
        return OrderApprovalEventPayload.builder()
                .orderId(order.getId().getValue().toString())
                .restaurantId(order.getRestaurantId().getValue().toString())
                .restaurantOrderStatus(restaurantOrderStatus.name())
                .products(order.getItems().stream().map(orderItem ->
                        OrderApprovalEventProduct.builder()
                                .id(orderItem.getProduct().getId().getValue().toString())
                                .quantity(orderItem.getQuantity())
                                .build()).collect(Collectors.toList()))
                .price(order.getPrice().getAmount())
                .createdAt(java.time.ZonedDateTime.now(java.time.ZoneId.of(com.berkay.domain.DomainConstants.UTC)))
                .build();
    }

    public Customer customerModelToCustomer(CustomerModel customerModel) {
        return new Customer(new CustomerId(UUID.fromString(customerModel.getId())),
                customerModel.getUsername(),
                customerModel.getFirstName(),
                customerModel.getLastName(),
                customerModel.getEmail());
    }

    private List<OrderItem> orderItemsToOrderItemEntities(List<com.berkay.order.service.domain.dto.create.OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem ->
                        OrderItem.builder()
                                .product(new Product(new ProductId(orderItem.getProductId())))
                                .price(new Money(orderItem.getPrice()))
                                .quantity(orderItem.getQuantity())
                                .subTotal(new Money(orderItem.getSubTotal()))
                                .build()).collect(Collectors.toList());
    }

    private StreetAddress orderAddressToStreetAddress(OrderAddress orderAddress) {
        return new StreetAddress(
                UUID.randomUUID(),
                orderAddress.getCity(),
                orderAddress.getDistrict(),
                orderAddress.getNeighborhood(),
                orderAddress.getStreet(),
                orderAddress.getBuildingNumber(),
                orderAddress.getDoorNumber(),
                orderAddress.getFloor(),
                orderAddress.getAddressInstructions(),
                orderAddress.getContactFirstName(),
                orderAddress.getContactLastName(),
                orderAddress.getContactPhone()
        );
    }

    public com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse orderToRestaurantOrderResponse(Order order) {
        return com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse.builder()
                .orderId(order.getId().getValue())
                .trackingId(order.getTrackingId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .orderStatus(order.getOrderStatus())
                .price(order.getPrice().getAmount())
                .deliveryFee(order.getDeliveryFee() != null ? order.getDeliveryFee().getAmount() : java.math.BigDecimal.ZERO)
                .createdAt(order.getCreatedAt())
                .items(order.getItems() != null ? order.getItems().stream()
                        .map(this::orderItemToRestaurantOrderItemResponse)
                        .collect(Collectors.toList()) : java.util.List.of())
                .deliveryAddress(streetAddressToDeliveryAddressResponse(order.getDeliveryAddress()))
                .cancelReason(order.getCancelReason())
                .materialAction(order.getMaterialAction())
                .internalNote(order.getInternalNote())
                .build();
    }

    public com.berkay.order.service.domain.dto.read.CustomerOrderDetailResponse orderToCustomerOrderDetailResponse(Order order, String restaurantName) {
        return com.berkay.order.service.domain.dto.read.CustomerOrderDetailResponse.builder()
                .orderId(order.getId().getValue())
                .trackingId(order.getTrackingId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .restaurantName(restaurantName)
                .orderStatus(order.getOrderStatus())
                .price(order.getPrice().getAmount())
                .deliveryFee(order.getDeliveryFee() != null ? order.getDeliveryFee().getAmount() : java.math.BigDecimal.ZERO)
                .createdAt(order.getCreatedAt())
                .items(order.getItems() != null ? order.getItems().stream()
                        .map(this::orderItemToRestaurantOrderItemResponse)
                        .collect(Collectors.toList()) : java.util.List.of())
                .deliveryAddress(streetAddressToDeliveryAddressResponse(order.getDeliveryAddress()))
                .cancelReason(order.getCancelReason())
                .build();
    }

    public com.berkay.order.service.domain.dto.read.CustomerOrderDetailResponse orderToCustomerOrderDetailResponse(Order order) {
        return orderToCustomerOrderDetailResponse(order, null);
    }

    public com.berkay.order.service.domain.dto.restaurant.RestaurantOrderItemResponse orderItemToRestaurantOrderItemResponse(OrderItem orderItem) {
        String productName = orderItem.getProduct() != null && orderItem.getProduct().getName() != null
                ? orderItem.getProduct().getName()
                : "Product";
        return com.berkay.order.service.domain.dto.restaurant.RestaurantOrderItemResponse.builder()
                .productId(orderItem.getProduct().getId().getValue())
                .productName(productName)
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice().getAmount())
                .subTotal(orderItem.getSubTotal().getAmount())
                .build();
    }

    public com.berkay.order.service.domain.dto.restaurant.DeliveryAddressResponse streetAddressToDeliveryAddressResponse(StreetAddress address) {
        if (address == null) {
            return null;
        }
        return com.berkay.order.service.domain.dto.restaurant.DeliveryAddressResponse.builder()
                .city(address.getCity())
                .district(address.getDistrict())
                .neighborhood(address.getNeighborhood())
                .street(address.getStreet())
                .buildingNumber(address.getBuildingNumber())
                .doorNumber(address.getDoorNumber())
                .floor(address.getFloor())
                .addressInstructions(address.getAddressInstructions())
                .contactFirstName(address.getContactFirstName())
                .contactLastName(address.getContactLastName())
                .contactPhone(address.getContactPhone())
                .build();
    }
}
