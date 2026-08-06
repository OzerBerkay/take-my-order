package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.RestaurantInformationEvent;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantPersonnelRepository;
import com.berkay.restaurant.service.domain.entity.RestaurantPersonnel;
import com.berkay.restaurant.service.domain.valueobject.RestaurantPersonnelId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxEventType.RESTAURANT_CREATED;

@Slf4j
@Component
public class CreateRestaurantCommandHandler {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantDataMapper restaurantDataMapper;
    private final RestaurantOutboxHelper restaurantOutboxHelper;
    private final RestaurantDomainService restaurantDomainService;
    private final RestaurantPersonnelRepository restaurantPersonnelRepository;

    public CreateRestaurantCommandHandler(RestaurantRepository restaurantRepository,
                                          RestaurantDataMapper restaurantDataMapper,
                                          RestaurantOutboxHelper restaurantOutboxHelper,
                                          RestaurantDomainService restaurantDomainService,
                                          RestaurantPersonnelRepository restaurantPersonnelRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantDataMapper = restaurantDataMapper;
        this.restaurantOutboxHelper = restaurantOutboxHelper;
        this.restaurantDomainService = restaurantDomainService;
        this.restaurantPersonnelRepository = restaurantPersonnelRepository;
    }

    @Transactional
    public CreateRestaurantResponse createRestaurant(CreateRestaurantCommand createRestaurantCommand) {
        log.info("Creating restaurant with name: {}", createRestaurantCommand.getRestaurantName());
        Restaurant restaurant = restaurantDataMapper.createRestaurantCommandToRestaurant(createRestaurantCommand);

        // Domain Logic (Validation + Init)
        // Event dönüyor ve entity initialize ediliyor
        RestaurantInformationEvent restaurantInformationEvent = restaurantDomainService.validateAndInitiateRestaurant(restaurant);

        // Veritabanı Kaydı
        Restaurant savedRestaurant = restaurantRepository.saveRestaurant(restaurant);

        if (savedRestaurant == null) {
            log.error("Could not save restaurant with name: {}", createRestaurantCommand.getRestaurantName());
            throw new RestaurantDomainException("Could not save restaurant with name: " +
                    createRestaurantCommand.getRestaurantName());
        }

        // Outbox Kaydı
        restaurantOutboxHelper.saveRestaurantOutboxMessage(restaurantInformationEvent, RESTAURANT_CREATED, createRestaurantCommand.getMerchantId());

        // RestaurantPersonnel Kaydı
        RestaurantPersonnel personnel = RestaurantPersonnel.builder()
                .restaurantPersonnelId(new RestaurantPersonnelId(java.util.UUID.randomUUID()))
                .restaurantId(savedRestaurant.getId())
                .userId(java.util.UUID.fromString(createRestaurantCommand.getMerchantId()))
                .createdAt(java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC")))
                .build();
        restaurantPersonnelRepository.save(personnel);

        log.info("Restaurant is created with id: {}", savedRestaurant.getId().getValue());
        return restaurantDataMapper.restaurantToCreateRestaurantResponse(savedRestaurant);
    }
}