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
                                        productEntity.isAvailable(),
                                        productEntity.isHidden()))
                        .collect(Collectors.toList()))
                .active(restaurantEntity.isRestaurantActive())
                .available(restaurantEntity.isAvailable())
                .minimumOrderAmount(restaurantEntity.getMinimumOrderAmount() != null ? new Money(restaurantEntity.getMinimumOrderAmount()) : null)
                .deliveryFee(restaurantEntity.getDeliveryFee() != null ? new Money(restaurantEntity.getDeliveryFee()) : null)
                .build();
    }

    public RestaurantEntity restaurantToRestaurantEntity(Restaurant restaurant) {
        RestaurantEntity restaurantEntity = RestaurantEntity.builder()
                .restaurantId(restaurant.getId().getValue())
                .name(restaurant.getName())
                .restaurantActive(restaurant.isActive())
                .available(restaurant.isAvailable())
                .minimumOrderAmount(restaurant.getMinimumOrderAmount() != null ? restaurant.getMinimumOrderAmount().getAmount() : null)
                .deliveryFee(restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee().getAmount() : null)
                .products(restaurant.getProducts().stream()
                        .map(product -> ProductEntity.builder()
                                .productId(product.getId().getValue())
                                .name(product.getName())
                                .price(product.getPrice().getAmount())
                                .available(product.isAvailable())
                                .hidden(product.isHidden())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        // Çift yönlü ilişkiyi kuruyoruz
        // Eğer bunu yapmazsak JPA bunu kaydetmeye çalıştığında, veritabanındaki products tablosunun restaurant_id kolonuna NULL yazmaya çalışacaktı
        restaurantEntity.getProducts().forEach(productEntity -> productEntity.setRestaurant(restaurantEntity));

        return restaurantEntity;
    }
}
