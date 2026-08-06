package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateRestaurantCommand;
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
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;

import static com.berkay.domain.DomainConstants.UTC;
import static com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxEventType.RESTAURANT_UPDATED;

@Slf4j
@Component
public class UpdateRestaurantCommandHandler {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantOutboxHelper restaurantOutboxHelper;
    private final RestaurantDataMapper restaurantDataMapper;

    public UpdateRestaurantCommandHandler(RestaurantRepository restaurantRepository,
                                          RestaurantOutboxHelper restaurantOutboxHelper,
                                          RestaurantDataMapper restaurantDataMapper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantOutboxHelper = restaurantOutboxHelper;
        this.restaurantDataMapper = restaurantDataMapper;
    }

    @Transactional
    public void updateRestaurant(UpdateRestaurantCommand command) {
        // Restoranı bul
        Restaurant restaurant = restaurantRepository.findRestaurantById(command.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + command.getRestaurantId()));


        restaurant.updateName(command.getRestaurantName());
        restaurant.updateActiveStatus(command.getActive());

        // Kaydet
        Restaurant savedRestaurant = restaurantRepository.saveRestaurant(restaurant);

        // OUTBOX - Order Service'i haberdar et
        // Restoranın son halini (Snapshot) gönderiyoruz.
        restaurantOutboxHelper.saveRestaurantOutboxMessage(
                restaurantDataMapper.restaurantToRestaurantInformationEvent(savedRestaurant),
                RESTAURANT_UPDATED,
                null
        );

        log.info("Restaurant id: {} updated and outbox message saved.", savedRestaurant.getId().getValue());
    }
}
