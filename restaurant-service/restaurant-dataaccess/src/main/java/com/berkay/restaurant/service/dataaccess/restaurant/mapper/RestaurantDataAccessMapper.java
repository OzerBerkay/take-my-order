package com.berkay.restaurant.service.dataaccess.restaurant.mapper;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.ProductId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.dataaccess.restaurant.entity.OrderApprovalEntity;
import com.berkay.restaurant.service.dataaccess.restaurant.entity.ProductEntity;
import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.berkay.restaurant.service.domain.entity.OrderApproval;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.valueobject.OrderApprovalId;
import com.berkay.restaurant.service.domain.valueobject.RestaurantName;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class RestaurantDataAccessMapper {

    // Domain -> JPA (Restoran Kaydetme)
    public RestaurantEntity restaurantToRestaurantEntity(Restaurant restaurant) {
        RestaurantEntity restaurantEntity = RestaurantEntity.builder()
                .restaurantId(restaurant.getId().getValue())
                .restaurantName(restaurant.getRestaurantName().getRestaurantName()) // VO'dan String'e
                .isActive(restaurant.isActive())
                .menu(new ArrayList<>()) // İlişkiyi aşağıda kuracağız
                .build();

        // Menüdeki her ürünü JPA Entity'sine çevir ve restoranla ilişkilendir
        restaurant.getMenu().forEach(product -> {
            restaurantEntity.getMenu().add(ProductEntity.builder()
                    .productId(product.getId().getValue())
                    .name(product.getName())
                    .price(product.getPrice().getAmount())
                    .stock(product.getStock())
                    .available(product.isAvailable())
                    .restaurant(restaurantEntity) // İlişkiyi kuruyoruz
                    .build());
        });

        return restaurantEntity;
    }

    // JPA -> Domain (Veri Okuma)
    public Restaurant restaurantEntityToRestaurant(RestaurantEntity restaurantEntity) {
        return Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantEntity.getRestaurantId()))
                .restaurantName(new RestaurantName(restaurantEntity.getRestaurantName()))
                .active(restaurantEntity.isActive())
                .menu(restaurantEntity.getMenu().stream()
                        .map(productEntity -> Product.builder()
                                .productId(new ProductId(productEntity.getProductId()))
                                .name(productEntity.getName())
                                .price(new Money(productEntity.getPrice()))
                                .stock(productEntity.getStock())
                                .available(productEntity.isAvailable())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public OrderApprovalEntity orderApprovalToOrderApprovalEntity(OrderApproval orderApproval) {
        return OrderApprovalEntity.builder()
                .id(orderApproval.getId().getValue())
                .restaurantId(orderApproval.getRestaurantId().getValue())
                .orderId(orderApproval.getOrderId().getValue())
                .status(orderApproval.getApprovalStatus())
                .build();
    }

    public OrderApproval orderApprovalEntityToOrderApproval(OrderApprovalEntity orderApprovalEntity) {
        return OrderApproval.builder()
                .orderApprovalId(new OrderApprovalId(orderApprovalEntity.getId()))
                .restaurantId(new RestaurantId(orderApprovalEntity.getRestaurantId()))
                .orderId(new OrderId(orderApprovalEntity.getOrderId()))
                .approvalStatus(orderApprovalEntity.getStatus())
                .build();
    }

}
