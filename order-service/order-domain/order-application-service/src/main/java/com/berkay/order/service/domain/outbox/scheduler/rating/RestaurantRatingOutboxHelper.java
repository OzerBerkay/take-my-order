package com.berkay.order.service.domain.outbox.scheduler.rating;

import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingEventPayload;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRatingOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class RestaurantRatingOutboxHelper {

    public static final String RESTAURANT_RATING_EVENT_TYPE = "RestaurantRating";

    private final RestaurantRatingOutboxRepository repository;
    private final ObjectMapper objectMapper;

    public RestaurantRatingOutboxHelper(RestaurantRatingOutboxRepository repository,
                                        ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<List<RestaurantRatingOutboxMessage>> getRestaurantRatingOutboxMessagesByOutboxStatus(OutboxStatus outboxStatus) {
        return repository.findByTypeAndOutboxStatus(RESTAURANT_RATING_EVENT_TYPE, outboxStatus);
    }

    @Transactional
    public void save(RestaurantRatingOutboxMessage message) {
        RestaurantRatingOutboxMessage saved = repository.save(message);
        if (saved == null) {
            log.error("Could not save RestaurantRatingOutboxMessage with id: {}", message.getId());
            throw new OrderDomainException("Could not save RestaurantRatingOutboxMessage with id: " + message.getId());
        }
        log.info("RestaurantRatingOutboxMessage saved with id: {} for restaurant: {}", message.getId(), message.getRestaurantId());
    }

    @Transactional
    public void saveRatingOutboxMessage(RestaurantRatingEventPayload payload, OutboxStatus outboxStatus) {
        UUID restaurantId = UUID.fromString(payload.getRestaurantId());
        Optional<RestaurantRatingOutboxMessage> existingPendingMessage =
                repository.findByTypeAndRestaurantIdAndOutboxStatus(RESTAURANT_RATING_EVENT_TYPE, restaurantId, OutboxStatus.STARTED);

        if (existingPendingMessage.isPresent()) {
            RestaurantRatingOutboxMessage message = existingPendingMessage.get();
            message.setPayload(createPayload(payload));
            message.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")));
            message.setProcessedAt(null);
            save(message);
            log.info("Updated existing pending RestaurantRatingOutboxMessage for restaurant: {}", restaurantId);
        } else {
            save(RestaurantRatingOutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .restaurantId(restaurantId)
                    .createdAt(payload.getUpdatedAt() != null ? payload.getUpdatedAt() : ZonedDateTime.now(ZoneId.of("UTC")))
                    .type(RESTAURANT_RATING_EVENT_TYPE)
                    .payload(createPayload(payload))
                    .outboxStatus(outboxStatus)
                    .build());
        }
    }

    @Transactional
    public void deleteRestaurantRatingOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        repository.deleteByTypeAndOutboxStatus(RESTAURANT_RATING_EVENT_TYPE, outboxStatus);
    }

    private String createPayload(RestaurantRatingEventPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Could not create RestaurantRatingEventPayload object for restaurant id: {}", payload.getRestaurantId(), e);
            throw new OrderDomainException("Could not create RestaurantRatingEventPayload object for restaurant id: " + payload.getRestaurantId(), e);
        }
    }
}
