package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.message.RestaurantRatingUpdateModel;
import com.berkay.restaurant.service.domain.ports.input.message.listener.RestaurantRatingMessageListener;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
public class RestaurantRatingMessageListenerImpl implements RestaurantRatingMessageListener {

    private final RestaurantRepository restaurantRepository;

    public RestaurantRatingMessageListenerImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    @Transactional
    public void updateRating(RestaurantRatingUpdateModel model) {
        if (model == null || model.getRestaurantId() == null) {
            log.warn("Received empty or invalid RestaurantRatingUpdateModel, skipping.");
            return;
        }

        UUID restaurantId = UUID.fromString(model.getRestaurantId());
        BigDecimal averageRating = model.getAverageRating() != null 
                ? BigDecimal.valueOf(model.getAverageRating()).setScale(1, RoundingMode.HALF_UP) 
                : null;
        Integer totalReviews = model.getTotalReviews();
        ZonedDateTime updatedAt = model.getUpdatedAt() != null 
                ? ZonedDateTime.ofInstant(model.getUpdatedAt(), ZoneId.of("UTC"))
                : ZonedDateTime.now(ZoneId.of("UTC"));

        log.info("Updating rating for restaurantId: {}, averageRating: {}, totalReviews: {}, updatedAt: {}",
                restaurantId, averageRating, totalReviews, updatedAt);

        int updatedRows = restaurantRepository.updateRestaurantRating(restaurantId, averageRating, totalReviews, updatedAt);
        if (updatedRows == 0) {
            log.warn("Restaurant rating was NOT updated for restaurantId: {}. It may either not exist or the update is older than existing rating_updated_at (stale event discarded).",
                    restaurantId);
        } else {
            log.info("Successfully updated rating for restaurantId: {}", restaurantId);
        }
    }
}
