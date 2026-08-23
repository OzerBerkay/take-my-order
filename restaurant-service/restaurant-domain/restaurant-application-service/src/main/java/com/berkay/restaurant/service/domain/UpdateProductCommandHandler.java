package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.restaurant.service.domain.dto.update.product.UpdateProductCommand;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.RestaurantInformationEvent;
import com.berkay.restaurant.service.domain.exception.ProductNotFoundException;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxEventType.RESTAURANT_UPDATED;

@Slf4j
@Component
public class UpdateProductCommandHandler {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantOutboxHelper restaurantOutboxHelper;
    private final RestaurantDataMapper restaurantDataMapper;

    public UpdateProductCommandHandler(RestaurantRepository restaurantRepository,
                                       RestaurantOutboxHelper restaurantOutboxHelper,
                                       RestaurantDataMapper restaurantDataMapper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantOutboxHelper = restaurantOutboxHelper;
        this.restaurantDataMapper = restaurantDataMapper;
    }

    @Transactional
    public void updateProduct(UpdateProductCommand command) {
        // Restoranı bul
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantById(command.getRestaurantId());
        if (restaurantResult.isEmpty()) {
            throw new RestaurantNotFoundException("Restaurant not found: " + command.getRestaurantId());
        }
        Restaurant restaurant = restaurantResult.get();

        // Ürünü bul ve güncelle
        Optional<Product> productOptional = restaurant.getMenu().stream()
                .filter(p -> p.getId().getValue().equals(command.getProductId()))
                .findFirst();

        if (productOptional.isEmpty()) {
            log.error("Product not found with id: {} in restaurant: {}", command.getProductId(), command.getRestaurantId());
            throw new ProductNotFoundException("Product not found with id: " + command.getProductId());
        }

        Product product = productOptional.get();

        product.updateWith(
                command.getName(),
                new Money(command.getPrice()),
                command.getAvailable(),
                command.getStock(),
                command.getHidden(),
                command.getImageUrl()
        );

        log.info("Product updated with id: {}", product.getId().getValue());

        // Kaydet ve DÖNEN OBJEYİ AL (@Version tutarlılığı için)
        Restaurant savedRestaurant = restaurantRepository.saveRestaurant(restaurant);

        // Restoranı Kaydet (JPA cascade ile product da güncellenir)
        restaurantRepository.saveRestaurant(restaurant);

        restaurantOutboxHelper.saveRestaurantOutboxMessage(
                restaurantDataMapper.restaurantToRestaurantInformationEvent(savedRestaurant),
                RESTAURANT_UPDATED,
                null
        );

        log.info("Restaurant menu updated and outbox message saved for restaurant id: {}", savedRestaurant.getId().getValue());
    }
}