package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.ProductId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.domain.dto.message.RestaurantModel;
import com.berkay.order.service.domain.entity.Product;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.order.service.domain.ports.input.message.listener.restaurant.RestaurantInformationMessageListener;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RestaurantInformationMessageListenerImpl implements RestaurantInformationMessageListener {

    private final RestaurantRepository restaurantRepository;

    public RestaurantInformationMessageListenerImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    @Transactional
    public void restaurantInformationReceived(RestaurantModel restaurantModel) {
        // DTO (RestaurantModel) -> Domain Entity (Restaurant) Dönüşümü
        List<Product> products = restaurantModel.getProducts().stream()
                .map(productModel -> new Product(
                        new ProductId(productModel.getProductId()),
                        productModel.getName(),
                        new Money(productModel.getPrice()),
                        productModel.isAvailable()))
                .collect(Collectors.toList());

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantModel.getRestaurantId()))
                .name(restaurantModel.getName())
                .active(restaurantModel.isActive())
                .products(products)
                .build();

        // Veritabanına Kayıt (Replica Table)
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        log.info("Restaurant is saved in Order Service with id: {}", savedRestaurant.getId().getValue());
    }
}
