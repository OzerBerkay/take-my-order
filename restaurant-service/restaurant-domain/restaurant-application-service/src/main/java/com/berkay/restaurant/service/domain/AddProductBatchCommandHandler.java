package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductBatchCommand;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductBatchResponse;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxEventType;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AddProductBatchCommandHandler {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantDataMapper restaurantDataMapper;
    private final RestaurantOutboxHelper restaurantOutboxHelper;

    public AddProductBatchCommandHandler(RestaurantRepository restaurantRepository,
                                         RestaurantDataMapper restaurantDataMapper,
                                         RestaurantOutboxHelper restaurantOutboxHelper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantDataMapper = restaurantDataMapper;
        this.restaurantOutboxHelper = restaurantOutboxHelper;
    }

    @Transactional
    public AddProductBatchResponse addProductBatch(AddProductBatchCommand command) {
        Restaurant restaurant = restaurantRepository.findRestaurantById(command.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + command.getRestaurantId()));

        List<Product> productsToAdd = command.getProducts().stream()
                .map(p -> Product.builder()
                        .name(p.getName())
                        .description(p.getDescription())
                        .price(new com.berkay.domain.valueobject.Money(p.getPrice()))
                        .stock(p.getStock())
                        .available(p.getAvailable())
                        .hidden(p.getHidden())
                        .imageUrl(p.getImageUrl())
                        .categoryId(p.getCategoryId() != null ? new com.berkay.domain.valueobject.ProductCategoryId(p.getCategoryId()) : null)
                        .build())
                .collect(Collectors.toList());

        for (Product product : productsToAdd) {
            restaurant.addProduct(product);
        }

        Restaurant savedRestaurant = restaurantRepository.saveRestaurant(restaurant);
        log.info("Batch added {} products to restaurant id: {}", productsToAdd.size(), restaurant.getId().getValue());

        restaurantOutboxHelper.saveRestaurantOutboxMessage(
                restaurantDataMapper.restaurantToRestaurantInformationEvent(savedRestaurant),
                RestaurantOutboxEventType.RESTAURANT_UPDATED,
                null
        );

        List<java.util.UUID> productIds = productsToAdd.stream().map(p -> p.getId().getValue()).collect(Collectors.toList());
        return AddProductBatchResponse.builder()
                .productIds(productIds)
                .message("Products added successfully")
                .build();
    }
}
