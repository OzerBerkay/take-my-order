package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.restaurant.service.domain.dto.update.UpdateProductCommand;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.RestaurantInformationEvent;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.berkay.domain.DomainConstants.UTC;
import static com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxEventType.RESTAURANT_UPDATED;

@Slf4j
@Component
public class UpdateProductCommandHandler {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantOutboxHelper restaurantOutboxHelper;

    public UpdateProductCommandHandler(RestaurantRepository restaurantRepository,
                                       RestaurantOutboxHelper restaurantOutboxHelper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantOutboxHelper = restaurantOutboxHelper;
    }

    @Transactional
    public void updateProduct(UpdateProductCommand command) {
        // Restoranı bul
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantById(command.getRestaurantId());
        if (restaurantResult.isEmpty()) {
            throw new RestaurantNotFoundException("Restaurant not found: " + command.getRestaurantId());
        }
        Restaurant restaurant = restaurantResult.get();

        // 2. Ürünü bul ve güncelle
        Optional<Product> productOptional = restaurant.getMenu().stream()
                .filter(p -> p.getId().getValue().equals(command.getProductId()))
                .findFirst();

        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.updateWith(command.getName(), new Money(command.getPrice()), command.isAvailable());
            log.info("Product updated with id: {}", product.getId().getValue());
        } else {
            // İstenirse belki burada yeni ürün eklenebilir.
            log.warn("Product not found with id: {}", command.getProductId());
        }

        // Restoranı Kaydet (JPA cascade ile product da güncellenir)
        restaurantRepository.saveRestaurant(restaurant);

        // OUTBOX - Order Service'e güncel menüyü gönder
        restaurantOutboxHelper.saveRestaurantOutboxMessage(
                new RestaurantInformationEvent(restaurant, ZonedDateTime.now(ZoneId.of(UTC))),
                RESTAURANT_UPDATED
        );

        log.info("Restaurant menu updated and outbox message saved for restaurant id: {}", restaurant.getId().getValue());
    }
}