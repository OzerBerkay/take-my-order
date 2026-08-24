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
    private final com.berkay.restaurant.service.domain.ports.output.repository.cuisine.CuisineRepository cuisineRepository;

    public UpdateRestaurantCommandHandler(RestaurantRepository restaurantRepository,
                                          RestaurantOutboxHelper restaurantOutboxHelper,
                                          RestaurantDataMapper restaurantDataMapper,
                                          com.berkay.restaurant.service.domain.ports.output.repository.cuisine.CuisineRepository cuisineRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantOutboxHelper = restaurantOutboxHelper;
        this.restaurantDataMapper = restaurantDataMapper;
        this.cuisineRepository = cuisineRepository;
    }

    @Transactional
    public void updateRestaurant(UpdateRestaurantCommand command) {
        // Restoranı bul
        Restaurant restaurant = restaurantRepository.findRestaurantById(command.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + command.getRestaurantId()));


        restaurant.updateName(command.getRestaurantName());
        restaurant.updateActiveStatus(command.getActive());
        restaurant.updateAvailability(command.getAvailable());
        restaurant.updateMinimumOrderAmount(command.getMinimumOrderAmount() != null ? new com.berkay.domain.valueobject.Money(command.getMinimumOrderAmount()) : null);
        restaurant.updateDeliveryFee(command.getDeliveryFee() != null ? new com.berkay.domain.valueobject.Money(command.getDeliveryFee()) : null);
        
        if (command.getStreet() != null || command.getCity() != null || command.getPostalCode() != null) {
            String currentStreet = restaurant.getAddress() != null ? restaurant.getAddress().getStreet() : null;
            String currentCity = restaurant.getAddress() != null ? restaurant.getAddress().getCity() : null;
            String currentPostal = restaurant.getAddress() != null ? restaurant.getAddress().getPostalCode() : null;
            
            restaurant.updateAddress(new com.berkay.restaurant.service.domain.valueobject.Address(
                command.getStreet() != null ? command.getStreet() : currentStreet,
                command.getCity() != null ? command.getCity() : currentCity,
                command.getPostalCode() != null ? command.getPostalCode() : currentPostal
            ));
        }
        
        restaurant.updatePhoneNumber(command.getPhoneNumber());
        restaurant.updateAverageDeliveryTime(command.getAverageDeliveryTimeInMinutes());
        if (command.getCuisineIds() != null) {
            if (command.getCuisineIds().isEmpty()) {
                restaurant.clearCuisines();
            } else {
                java.util.List<com.berkay.restaurant.service.domain.entity.Cuisine> cuisines = new java.util.ArrayList<>();
                for (java.util.UUID cuisineId : command.getCuisineIds()) {
                    com.berkay.restaurant.service.domain.entity.Cuisine cuisine = cuisineRepository.findById(cuisineId)
                            .orElseThrow(() -> new com.berkay.restaurant.service.domain.exception.RestaurantDomainException("Cuisine not found with id: " + cuisineId));
                    cuisines.add(cuisine);
                }
                restaurant.updateCuisines(cuisines);
            }
        }
        restaurant.updateDescription(command.getDescription());
        restaurant.updateLogoUrl(command.getLogoUrl());

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
