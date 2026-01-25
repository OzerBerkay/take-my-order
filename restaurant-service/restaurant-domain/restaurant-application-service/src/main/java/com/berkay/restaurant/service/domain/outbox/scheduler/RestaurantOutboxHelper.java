package com.berkay.restaurant.service.domain.outbox.scheduler;

import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.event.RestaurantInformationEvent;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantEventPayload;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.berkay.domain.DomainConstants.UTC;
import static com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxEventType.RESTAURANT_CREATED;

@Slf4j
@Component
public class RestaurantOutboxHelper {

    private final RestaurantOutboxRepository restaurantOutboxRepository;
    private final ObjectMapper objectMapper;
    private final RestaurantDataMapper restaurantDataMapper;

    public RestaurantOutboxHelper(RestaurantOutboxRepository restaurantOutboxRepository,
                                  ObjectMapper objectMapper,
                                  RestaurantDataMapper restaurantDataMapper) {
        this.restaurantOutboxRepository = restaurantOutboxRepository;
        this.objectMapper = objectMapper;
        this.restaurantDataMapper = restaurantDataMapper;
    }

    @Transactional(readOnly = true)
    public Optional<List<RestaurantOutboxMessage>> getRestaurantOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        return restaurantOutboxRepository.findByTypeAndOutboxStatus(RESTAURANT_CREATED.name(), outboxStatus);
    }

    // Her bir outbox mesajının sürecini ayrı olarak değerlendirmek gerekir.
    // Bu sebeple de her birinin transaction'u kendine olmalıdır (fault-tolerant)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOutboxMessage(RestaurantOutboxMessage restaurantOutboxMessage, OutboxStatus outboxStatus) {
        restaurantOutboxMessage.setOutboxStatus(outboxStatus);
        restaurantOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        restaurantOutboxRepository.save(restaurantOutboxMessage);
        log.info("RestaurantOutboxMessage is updated with outbox status: {}", outboxStatus.name());
    }

    @Transactional
    public void saveRestaurantOutboxMessage(RestaurantInformationEvent restaurantInformationEvent) {
        RestaurantEventPayload payload = restaurantDataMapper.restaurantInformationEventToRestaurantEventPayload(restaurantInformationEvent);
        try {
            RestaurantOutboxMessage restaurantOutboxMessage = RestaurantOutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .createdAt(payload.getCreatedAt())
                    .type(RESTAURANT_CREATED.name())
                    .payload(objectMapper.writeValueAsString(payload))
                    .outboxStatus(OutboxStatus.STARTED)
                    .version(0)
                    .build();

            restaurantOutboxRepository.save(restaurantOutboxMessage);
            log.info("RestaurantOutboxMessage saved with id: {}", restaurantOutboxMessage.getId());

        } catch (JsonProcessingException e) {
            log.error("Could not create RestaurantOutboxMessage for restaurant id: {}", payload.getRestaurantId(), e);
            throw new RestaurantDomainException("Could not create RestaurantOutboxMessage", e);
        }
    }
}