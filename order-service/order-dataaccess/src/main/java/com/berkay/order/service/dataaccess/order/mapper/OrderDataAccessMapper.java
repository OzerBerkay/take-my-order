package com.berkay.order.service.dataaccess.order.mapper;

import com.berkay.domain.valueobject.*;
import com.berkay.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.berkay.order.service.dataaccess.order.entity.OrderEntity;
import com.berkay.order.service.dataaccess.order.entity.OrderItemEntity;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.entity.OrderItem;
import com.berkay.order.service.domain.entity.Product;
import com.berkay.order.service.domain.valueobject.OrderItemId;
import com.berkay.order.service.domain.valueobject.StreetAddress;
import com.berkay.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderDataAccessMapper {

    public OrderEntity orderToOrderEntity(Order order) {
        OrderEntity orderEntity = OrderEntity.builder()
                .id(order.getId().getValue())
                .customerId(order.getCustomerId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .trackingId(order.getTrackingId().getValue())
                .address(deliveryAddressToAddressEntity(order.getDeliveryAddress()))
                .price(order.getPrice().getAmount())
                .deliveryFee(order.getDeliveryFee() != null ? order.getDeliveryFee().getAmount() : BigDecimal.ZERO)
                .items(orderItemsToOrderItemEntities(order.getItems()))
                .orderStatus(order.getOrderStatus())
                .cancelReason(order.getCancelReason())
                .materialAction(order.getMaterialAction())
                .internalNote(order.getInternalNote())
                .version(order.getVersion())
                .createdAt(order.getCreatedAt())
                .build();
        orderEntity.getAddress().setOrder(orderEntity);
        orderEntity.getItems().forEach(orderItemEntity -> orderItemEntity.setOrder(orderEntity));

        return orderEntity;
    }

    public Order orderEntityToOrder(OrderEntity orderEntity) {
        return Order.builder()
                .orderId(new OrderId(orderEntity.getId()))
                .customerId(new CustomerId(orderEntity.getCustomerId()))
                .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
                .deliveryAddress(addressEntityToDeliveryAddress(orderEntity.getAddress()))
                .price(new Money(orderEntity.getPrice()))
                .deliveryFee(orderEntity.getDeliveryFee() != null ? new Money(orderEntity.getDeliveryFee()) : Money.ZERO)
                .items(orderItemEntitiesToOrderItems(orderEntity.getItems()))
                .trackingId(new TrackingId(orderEntity.getTrackingId()))
                .orderStatus(orderEntity.getOrderStatus())
                .cancelReason(orderEntity.getCancelReason())
                .materialAction(orderEntity.getMaterialAction())
                .internalNote(orderEntity.getInternalNote())
                .version(orderEntity.getVersion())
                .createdAt(orderEntity.getCreatedAt())
                .build();
    }

    private List<OrderItem> orderItemEntitiesToOrderItems(List<OrderItemEntity> items) {
        return items.stream()
                .map(orderItemEntity -> OrderItem.builder()
                        .orderItemId(new OrderItemId(orderItemEntity.getId()))
                        .product(new Product(new ProductId(orderItemEntity.getProductId()), orderItemEntity.getProductName(), new Money(orderItemEntity.getPrice()), true, false))
                        .price(new Money(orderItemEntity.getPrice()))
                        .quantity(orderItemEntity.getQuantity())
                        .subTotal(new Money(orderItemEntity.getSubTotal()))
                        .build())
                .collect(Collectors.toList());
    }

    private StreetAddress addressEntityToDeliveryAddress(OrderAddressEntity address) {
        return new StreetAddress(address.getId(),
                address.getCity(),
                address.getDistrict(),
                address.getNeighborhood(),
                address.getStreet(),
                address.getBuildingNumber(),
                address.getDoorNumber(),
                address.getFloor(),
                address.getAddressInstructions(),
                address.getContactFirstName(),
                address.getContactLastName(),
                address.getContactPhone());
    }

    private List<OrderItemEntity> orderItemsToOrderItemEntities(List<OrderItem> items) {
        return items.stream()
                .map(orderItem -> OrderItemEntity.builder()
                        .id(orderItem.getId().getValue())
                        .productId(orderItem.getProduct().getId().getValue())
                        .productName(orderItem.getProduct().getName() != null ? orderItem.getProduct().getName() : "Product")
                        .price(orderItem.getPrice().getAmount())
                        .quantity(orderItem.getQuantity())
                        .subTotal(orderItem.getSubTotal().getAmount())
                        .build())
                .collect(Collectors.toList());
    }

    private OrderAddressEntity deliveryAddressToAddressEntity(StreetAddress deliveryAddress) {
        return OrderAddressEntity.builder()
                .id(deliveryAddress.getId())
                .city(deliveryAddress.getCity())
                .district(deliveryAddress.getDistrict())
                .neighborhood(deliveryAddress.getNeighborhood())
                .street(deliveryAddress.getStreet())
                .buildingNumber(deliveryAddress.getBuildingNumber())
                .doorNumber(deliveryAddress.getDoorNumber())
                .floor(deliveryAddress.getFloor())
                .addressInstructions(deliveryAddress.getAddressInstructions())
                .contactFirstName(deliveryAddress.getContactFirstName())
                .contactLastName(deliveryAddress.getContactLastName())
                .contactPhone(deliveryAddress.getContactPhone())
                .build();
    }
}