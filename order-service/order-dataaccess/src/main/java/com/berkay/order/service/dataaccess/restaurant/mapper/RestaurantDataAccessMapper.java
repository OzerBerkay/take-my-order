package com.berkay.order.service.dataaccess.restaurant.mapper;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.ProductId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.dataaccess.restaurant.entity.ProductEntity;
import com.berkay.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.berkay.order.service.domain.entity.Product;
import com.berkay.order.service.domain.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RestaurantDataAccessMapper {

    public Restaurant restaurantEntityToRestaurant(RestaurantEntity restaurantEntity) {
        return Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantEntity.getRestaurantId()))
                .name(restaurantEntity.getName())
                .products(restaurantEntity.getProducts().stream().map(productEntity ->
                                new Product(new ProductId(productEntity.getProductId()),
                                        productEntity.getName(),
                                        new Money(productEntity.getPrice()),
                                        productEntity.isAvailable()))
                        .collect(Collectors.toList()))
                .active(restaurantEntity.isRestaurantActive())
                .build();
    }

    public RestaurantEntity restaurantToRestaurantEntity(Restaurant restaurant) {
        RestaurantEntity restaurantEntity = RestaurantEntity.builder()
                .restaurantId(restaurant.getId().getValue())
                .name(restaurant.getName())
                .restaurantActive(restaurant.isActive())
                .products(restaurant.getProducts().stream()
                        .map(product -> ProductEntity.builder()
                                .productId(product.getId().getValue())
                                .name(product.getName())
                                .price(product.getPrice().getAmount())
                                .available(product.isAvailable())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        // Çift yönlü ilişkiyi kuruyoruz
        restaurantEntity.getProducts().forEach(productEntity -> productEntity.setRestaurant(restaurantEntity));

        return restaurantEntity;
    }
}
