package com.berkay.restaurant.service.domain.outbox.scheduler;

import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.event.RestaurantInformationEvent;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantEventPayload;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxEventType;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantPersonnelEventPayload;
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
import java.util.UUID;

import static com.berkay.domain.DomainConstants.UTC;

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
    public List<RestaurantOutboxMessage> getRestaurantOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        return restaurantOutboxRepository.findByTypeAndOutboxStatus(
                List.of(RestaurantOutboxEventType.RESTAURANT_CREATED.name(),
                        RestaurantOutboxEventType.RESTAURANT_UPDATED.name()),
                outboxStatus);
    }

    @Transactional(readOnly = true)
    public List<RestaurantOutboxMessage> getPersonnelOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        return restaurantOutboxRepository.findByTypeAndOutboxStatus(
                List.of(RestaurantOutboxEventType.PERSONNEL_ADDED.name()),
                outboxStatus);
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
    public void saveRestaurantOutboxMessage(RestaurantInformationEvent restaurantInformationEvent,
                                            RestaurantOutboxEventType eventType,
                                            String merchantId) {
        RestaurantEventPayload payload = restaurantDataMapper.restaurantInformationEventToRestaurantEventPayload(restaurantInformationEvent, merchantId, eventType.name());
        try {
            RestaurantOutboxMessage restaurantOutboxMessage = RestaurantOutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .createdAt(payload.getCreatedAt())
                    .type(eventType.name())
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

    @Transactional
    public void savePersonnelOutboxMessage(RestaurantPersonnelEventPayload payload) {
        try {
            RestaurantOutboxMessage restaurantOutboxMessage = RestaurantOutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .createdAt(payload.getCreatedAt())
                    .type(RestaurantOutboxEventType.PERSONNEL_ADDED.name())
                    .payload(objectMapper.writeValueAsString(payload))
                    .outboxStatus(OutboxStatus.STARTED)
                    .version(0)
                    .build();

            restaurantOutboxRepository.save(restaurantOutboxMessage);
            log.info("RestaurantPersonnelOutboxMessage saved with id: {}", restaurantOutboxMessage.getId());

        } catch (JsonProcessingException e) {
            log.error("Could not create RestaurantPersonnelOutboxMessage for restaurant id: {} and user id: {}", payload.getRestaurantId(), payload.getUserId(), e);
            throw new RestaurantDomainException("Could not create RestaurantPersonnelOutboxMessage", e);
        }
    }

    @Transactional
    public void deleteRestaurantOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        restaurantOutboxRepository.deleteByTypeAndOutboxStatus(
                List.of(RestaurantOutboxEventType.RESTAURANT_CREATED.name(),
                        RestaurantOutboxEventType.RESTAURANT_UPDATED.name()),
                outboxStatus);
    }

    @Transactional
    public void deletePersonnelOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        restaurantOutboxRepository.deleteByTypeAndOutboxStatus(
                List.of(RestaurantOutboxEventType.PERSONNEL_ADDED.name()),
                outboxStatus);
    }
}