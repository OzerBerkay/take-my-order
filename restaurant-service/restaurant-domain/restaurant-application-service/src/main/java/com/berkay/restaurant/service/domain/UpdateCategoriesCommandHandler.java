package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateCategoriesCommand;
import com.berkay.restaurant.service.domain.entity.ProductCategory;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UpdateCategoriesCommandHandler {

    private final RestaurantRepository restaurantRepository;

    public UpdateCategoriesCommandHandler(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateCategoriesResponse updateCategories(UpdateCategoriesCommand updateCategoriesCommand) {
        Restaurant restaurant = restaurantRepository.findRestaurantById(updateCategoriesCommand.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + updateCategoriesCommand.getRestaurantId()));

        List<ProductCategory> payloadCategories = updateCategoriesCommand.getCategories().stream()
                .map(payload -> ProductCategory.builder()
                        .productCategoryId(payload.getId() != null ? new com.berkay.domain.valueobject.ProductCategoryId(payload.getId()) : null)
                        .name(payload.getName())
                        .sortOrder(payload.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        restaurant.updateCategories(payloadCategories, updateCategoriesCommand.getCategoryVersion());

        Restaurant savedRestaurant = restaurantRepository.saveRestaurant(restaurant);
        log.info("Categories updated for restaurant id: {}", restaurant.getId().getValue());

        List<com.berkay.restaurant.service.domain.dto.read.ProductCategoryModel> categoryModels = savedRestaurant.getCategories().stream()
                .map(c -> com.berkay.restaurant.service.domain.dto.read.ProductCategoryModel.builder()
                        .id(c.getId().getValue())
                        .name(c.getName())
                        .sortOrder(c.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateCategoriesResponse.builder()
                .categories(categoryModels)
                .build();
    }
}
