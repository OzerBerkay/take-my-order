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
import java.util.Optional;
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
                        productModel.isAvailable(),
                        productModel.isHidden()))
                .collect(Collectors.toList());

        Optional<Restaurant> restaurantResult = restaurantRepository
                .findRestaurantByRestaurantId(restaurantModel.getRestaurantId());

        if (restaurantResult.isPresent()) {
            // UPDATE
            Restaurant restaurant = restaurantResult.get();
            restaurant.update(restaurantModel.getName(), restaurantModel.isActive(), restaurantModel.isAvailable(), products,
                    restaurantModel.getMinimumOrderAmount() != null ? new Money(restaurantModel.getMinimumOrderAmount()) : null,
                    restaurantModel.getDeliveryFee() != null ? new Money(restaurantModel.getDeliveryFee()) : null);

            restaurantRepository.save(restaurant);
            log.info("Restaurant is updated in Order Service with id: {}", restaurant.getId().getValue());

        } else {
            // CREATE
            Restaurant restaurant = Restaurant.builder()
                    .restaurantId(new RestaurantId(restaurantModel.getRestaurantId()))
                    .name(restaurantModel.getName())
                    .active(restaurantModel.isActive())
                    .available(restaurantModel.isAvailable())
                    .products(products)
                    .minimumOrderAmount(restaurantModel.getMinimumOrderAmount() != null ? new Money(restaurantModel.getMinimumOrderAmount()) : null)
                    .deliveryFee(restaurantModel.getDeliveryFee() != null ? new Money(restaurantModel.getDeliveryFee()) : null)
                    .build();

            // Veritabanına Kayıt (Replica Table)
            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            log.info("Restaurant is created in Order Service with id: {}", savedRestaurant.getId().getValue());
        }
    }
}
