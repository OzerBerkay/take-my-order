package com.berkay.restaurant.service.domain.mapper;

import com.berkay.domain.valueobject.*;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateProductCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.OrderApprovalEvent;
import com.berkay.restaurant.service.domain.event.RestaurantInformationEvent;
import com.berkay.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantEventPayload;
import com.berkay.restaurant.service.domain.valueobject.RestaurantName;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RestaurantDataMapper {

    public Restaurant createRestaurantCommandToRestaurant(CreateRestaurantCommand createRestaurantCommand) {
        return Restaurant.builder()
                // ID atanmıyor, Domain Service'deki initializeRestaurant metodunda atanacak.
                .restaurantName(new RestaurantName(createRestaurantCommand.getRestaurantName()))
                .active(createRestaurantCommand.isActive())
                .menu(createProductCommandsToProducts(createRestaurantCommand.getProducts()))
                .build();
    }

    private List<Product> createProductCommandsToProducts(List<CreateProductCommand> createProductCommands) {
        return createProductCommands.stream()
                .map(productCommand -> Product.builder()
                        .name(productCommand.getName())
                        .price(new Money(productCommand.getPrice()))
                        .stock(productCommand.getStock())
                        .available(productCommand.isAvailable())
                        .build())
                .collect(Collectors.toList());
    }

    public CreateRestaurantResponse restaurantToCreateRestaurantResponse(Restaurant restaurant) {
        return CreateRestaurantResponse.builder()
                .restaurantId(restaurant.getId().getValue())
                .message("Restaurant created successfully")
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

    public RestaurantEventPayload restaurantInformationEventToRestaurantEventPayload(RestaurantInformationEvent restaurantInformationEvent) {
        return RestaurantEventPayload.builder()
                .restaurantId(restaurantInformationEvent.getRestaurant().getId().getValue().toString())
                .active(restaurantInformationEvent.getRestaurant().isActive())
                .createdAt(restaurantInformationEvent.getCreatedAt())
                .products(restaurantInformationEvent.getRestaurant().getMenu().stream().map(product ->
                        RestaurantEventPayload.ProductPayload.builder()
                                .productId(product.getId().getValue().toString())
                                .name(product.getName())
                                .price(product.getPrice().getAmount())
                                .available(product.isAvailable())
                                .build()).toList())
                .build();
    }
}
