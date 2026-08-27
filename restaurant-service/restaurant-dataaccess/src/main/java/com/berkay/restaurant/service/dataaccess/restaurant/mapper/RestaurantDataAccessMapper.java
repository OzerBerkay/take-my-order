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
                .available(restaurant.isAvailable())
                .street(restaurant.getAddress() != null ? restaurant.getAddress().getStreet() : null)
                .city(restaurant.getAddress() != null ? restaurant.getAddress().getCity() : null)
                .postalCode(restaurant.getAddress() != null ? restaurant.getAddress().getPostalCode() : null)
                .phoneNumber(restaurant.getPhoneNumber())
                .minimumOrderAmount(restaurant.getMinimumOrderAmount() != null ? restaurant.getMinimumOrderAmount().getAmount() : null)
                .deliveryFee(restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee().getAmount() : null)
                .averageDeliveryTimeInMinutes(restaurant.getAverageDeliveryTimeInMinutes())
                .description(restaurant.getDescription())
                .logoUrl(restaurant.getLogoUrl())
                .bannerUrl(restaurant.getBannerUrl())
                .menu(new ArrayList<>()) // İlişkiyi aşağıda kuracağız
                .cuisines(restaurant.getCuisines() != null ? restaurant.getCuisines().stream()
                        .map(cuisine -> com.berkay.restaurant.service.dataaccess.restaurant.entity.CuisineEntity.builder()
                                .id(cuisine.getId().getValue())
                                .name(cuisine.getName())
                                .code(cuisine.getCode())
                                .description(cuisine.getDescription())
                                .iconUrl(cuisine.getIconUrl())
                                .isActive(cuisine.isActive())
                                .build())
                        .collect(Collectors.toSet()) : new java.util.HashSet<>())
                .build();

        restaurant.getMenu().forEach(product -> {
            restaurantEntity.getMenu().add(ProductEntity.builder()
                    .productId(product.getId().getValue())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice().getAmount())
                    .stock(product.getStock())
                    .available(product.isAvailable())
                    .hidden(product.isHidden())
                    .imageUrl(product.getImageUrl())
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
                .available(restaurantEntity.isAvailable())
                .address(new com.berkay.restaurant.service.domain.valueobject.Address(
                        restaurantEntity.getStreet(),
                        restaurantEntity.getCity(),
                        restaurantEntity.getPostalCode()
                ))
                .phoneNumber(restaurantEntity.getPhoneNumber())
                .minimumOrderAmount(restaurantEntity.getMinimumOrderAmount() != null ? new Money(restaurantEntity.getMinimumOrderAmount()) : null)
                .deliveryFee(restaurantEntity.getDeliveryFee() != null ? new Money(restaurantEntity.getDeliveryFee()) : null)
                .averageDeliveryTimeInMinutes(restaurantEntity.getAverageDeliveryTimeInMinutes())
                .description(restaurantEntity.getDescription())
                .logoUrl(restaurantEntity.getLogoUrl())
                .bannerUrl(restaurantEntity.getBannerUrl())
                .cuisines(restaurantEntity.getCuisines() != null ? restaurantEntity.getCuisines().stream()
                        .map(cuisineEntity -> com.berkay.restaurant.service.domain.entity.Cuisine.builder()
                                .cuisineId(new com.berkay.restaurant.service.domain.valueobject.CuisineId(cuisineEntity.getId()))
                                .name(cuisineEntity.getName())
                                .code(cuisineEntity.getCode())
                                .description(cuisineEntity.getDescription())
                                .iconUrl(cuisineEntity.getIconUrl())
                                .active(cuisineEntity.getIsActive())
                                .build())
                        .collect(Collectors.toList()) : new java.util.ArrayList<>())
                .menu(restaurantEntity.getMenu().stream()
                        .map(productEntity -> Product.builder()
                                .productId(new ProductId(productEntity.getProductId()))
                                .name(productEntity.getName())
                                .description(productEntity.getDescription())
                                .price(new Money(productEntity.getPrice()))
                                .stock(productEntity.getStock())
                                .available(productEntity.isAvailable())
                                .hidden(productEntity.isHidden())
                                .imageUrl(productEntity.getImageUrl())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public OrderApprovalEntity orderApprovalToOrderApprovalEntity(OrderApproval orderApproval) {
        java.util.Map<java.util.UUID, Integer> productQuantities = null;
        if (orderApproval.getProductQuantities() != null) {
            productQuantities = orderApproval.getProductQuantities().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().getValue(), java.util.Map.Entry::getValue));
        }
        return OrderApprovalEntity.builder()
                .id(orderApproval.getId().getValue())
                .restaurantId(orderApproval.getRestaurantId().getValue())
                .orderId(orderApproval.getOrderId().getValue())
                .status(orderApproval.getApprovalStatus())
                .productQuantities(productQuantities)
                .build();
    }

    public OrderApproval orderApprovalEntityToOrderApproval(OrderApprovalEntity orderApprovalEntity) {
        java.util.Map<ProductId, Integer> productQuantities = null;
        if (orderApprovalEntity.getProductQuantities() != null) {
            productQuantities = orderApprovalEntity.getProductQuantities().entrySet().stream()
                    .collect(Collectors.toMap(e -> new ProductId(e.getKey()), java.util.Map.Entry::getValue));
        }
        return OrderApproval.builder()
                .orderApprovalId(new OrderApprovalId(orderApprovalEntity.getId()))
                .restaurantId(new RestaurantId(orderApprovalEntity.getRestaurantId()))
                .orderId(new OrderId(orderApprovalEntity.getOrderId()))
                .approvalStatus(orderApprovalEntity.getStatus())
                .productQuantities(productQuantities)
                .build();
    }

}
