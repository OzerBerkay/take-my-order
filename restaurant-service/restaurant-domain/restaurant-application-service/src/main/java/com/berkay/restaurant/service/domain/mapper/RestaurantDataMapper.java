package com.berkay.restaurant.service.domain.mapper;

import com.berkay.domain.valueobject.*;
import com.berkay.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.berkay.restaurant.service.domain.dto.create.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.entity.OrderDetail;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.OrderApprovalEvent;
import com.berkay.restaurant.service.domain.event.RestaurantCreatedEvent;
import com.berkay.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantEventPayload;
import com.berkay.restaurant.service.domain.valueobject.RestaurantName;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantDataMapper {

    public Restaurant createRestaurantCommandToRestaurant(CreateRestaurantCommand createRestaurantCommand) {
        // ID initializeRestaurant içinde atanacak
        return Restaurant.builder()
                .restaurantName(new RestaurantName(createRestaurantCommand.getRestaurantName()))
                .build();
    }

    public CreateRestaurantResponse restaurantToCreateRestaurantResponse(Restaurant restaurant) {
        return CreateRestaurantResponse.builder()
                .restaurantId(restaurant.getId().getValue())
                .message("Restaurant created successfully")
                .build();
    }


    public Restaurant restaurantApprovalRequestToRestaurant(RestaurantApprovalRequest
                                                                    restaurantApprovalRequest) {
        return Restaurant.builder()
                .restaurantId(new RestaurantId(UUID.fromString(restaurantApprovalRequest.getRestaurantId())))
                .orderDetail(OrderDetail.builder()
                        .orderId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())))
                        .productQuantities(restaurantApprovalRequest.getProductQuantities().stream()
                                .collect(Collectors.toMap(
                                        item -> new ProductId(UUID.fromString(item.getId())),
                                        RestaurantApprovalRequest.ProductQuantity::getQuantity
                                )))
                        .totalAmount(new Money(restaurantApprovalRequest.getPrice()))
                        .orderStatus(OrderStatus.valueOf(restaurantApprovalRequest.getRestaurantOrderStatus().name()))
                        .build())
                .build();
    }

    public OrderEventPayload
    orderApprovalEventToOrderEventPayload(OrderApprovalEvent orderApprovalEvent) {
        return OrderEventPayload.builder()
                .orderId(orderApprovalEvent.getOrderApproval().getOrderId().getValue().toString())
                .restaurantId(orderApprovalEvent.getRestaurantId().getValue().toString())
                .orderApprovalStatus(orderApprovalEvent.getOrderApproval().getApprovalStatus().name())
                .createdAt(orderApprovalEvent.getCreatedAt())
                .failureMessages(orderApprovalEvent.getFailureMessages())
                .build();
    }

    public Product addProductCommandToProduct(AddProductCommand addProductCommand) {
        return Product.builder()
                .name(addProductCommand.getName())
                .price(new Money(addProductCommand.getPrice()))
                .stock(addProductCommand.getStock())
                .available(addProductCommand.isAvailable())
                .build();
    }

    public AddProductResponse productToAddProductResponse(Product product) {
        return AddProductResponse.builder()
                .productId(product.getId().getValue())
                .message("Product added successfully")
                .build();
    }

    public RestaurantEventPayload restaurantCreatedEventToRestaurantEventPayload(RestaurantCreatedEvent restaurantCreatedEvent) {
        return RestaurantEventPayload.builder()
                .restaurantId(restaurantCreatedEvent.getRestaurant().getId().getValue().toString())
                .active(restaurantCreatedEvent.getRestaurant().isActive())
                .createdAt(restaurantCreatedEvent.getCreatedAt())
                .products(restaurantCreatedEvent.getRestaurant().getMenu().stream().map(product ->
                        RestaurantEventPayload.ProductPayload.builder()
                                .productId(product.getId().getValue().toString())
                                .name(product.getName())
                                .price(product.getPrice().getAmount())
                                .available(product.isAvailable())
                                .build()).toList())
                .build();
    }
}
