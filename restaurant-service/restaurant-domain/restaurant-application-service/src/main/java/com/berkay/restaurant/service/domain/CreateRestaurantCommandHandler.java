package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class CreateRestaurantCommandHandler {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantDataMapper restaurantDataMapper;

    public CreateRestaurantCommandHandler(RestaurantRepository restaurantRepository,
                                          RestaurantDataMapper restaurantDataMapper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantDataMapper = restaurantDataMapper;
    }

    @Transactional
    public CreateRestaurantResponse createRestaurant(CreateRestaurantCommand createRestaurantCommand) {
        log.info("Creating restaurant with name: {}", createRestaurantCommand.getRestaurantName());
        Restaurant restaurant = restaurantDataMapper.createRestaurantCommandToRestaurant(createRestaurantCommand);
        restaurant.initializeRestaurant();
        Restaurant savedRestaurant = restaurantRepository.saveRestaurant(restaurant);

        if (savedRestaurant == null) {
            log.error("Could not save restaurant with name: {}", createRestaurantCommand.getRestaurantName());
            throw new RestaurantDomainException("Could not save restaurant with name: " +
                    createRestaurantCommand.getRestaurantName());
        }

        log.info("Restaurant is created with id: {}", savedRestaurant.getId().getValue());
        return restaurantDataMapper.restaurantToCreateRestaurantResponse(savedRestaurant);
    }
}