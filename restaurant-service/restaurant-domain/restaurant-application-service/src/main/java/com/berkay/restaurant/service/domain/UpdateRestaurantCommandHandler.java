package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.update.UpdateRestaurantCommand;
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

@Slf4j
@Component
public class UpdateRestaurantCommandHandler {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantOutboxHelper restaurantOutboxHelper;

    public UpdateRestaurantCommandHandler(RestaurantRepository restaurantRepository,
                                          RestaurantOutboxHelper restaurantOutboxHelper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantOutboxHelper = restaurantOutboxHelper;
    }

    @Transactional
    public void updateRestaurant(UpdateRestaurantCommand command) {
        // Restoranı bul
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantById(command.getRestaurantId());
        if (restaurantResult.isEmpty()) {
            throw new RestaurantNotFoundException("Restaurant not found: " + command.getRestaurantId());
        }
        Restaurant restaurant = restaurantResult.get();

        // Güncelle (Aktif/Pasif)
        restaurant.setActive(command.isActive());

        // Kaydet
        restaurantRepository.saveRestaurant(restaurant);

        // OUTBOX - Order Service'i haberdar et
        // Restoranın son halini (Snapshot) gönderiyoruz.
        restaurantOutboxHelper.saveRestaurantOutboxMessage(
                new RestaurantInformationEvent(restaurant, ZonedDateTime.now(ZoneId.of(UTC)))
        );

        log.info("Restaurant id: {} updated and outbox message saved.", restaurant.getId().getValue());
    }
}
