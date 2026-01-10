package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.create.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.AddProductResponse;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
public class AddProductCommandHandler {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantDataMapper restaurantDataMapper;

    public AddProductCommandHandler(RestaurantRepository restaurantRepository,
                                    RestaurantDataMapper restaurantDataMapper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantDataMapper = restaurantDataMapper;
    }

    @Transactional
    public AddProductResponse addProduct(AddProductCommand addProductCommand) {
        // 1. Restoranı Bul
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantById(addProductCommand.getRestaurantId());
        if (restaurantResult.isEmpty()) {
            log.error("Restaurant with id: {} not found!", addProductCommand.getRestaurantId());
            throw new RestaurantNotFoundException("Restaurant with id: " + addProductCommand.getRestaurantId() + " not found!");
        }
        Restaurant restaurant = restaurantResult.get();

        // 2. Ürünü Domain Entity'ye Çevir
        Product product = restaurantDataMapper.addProductCommandToProduct(addProductCommand);

        // 3. İş Kuralını İşlet (Domain Core'daki addProduct metodu)
        // (Bu metodun duplicate kontrolü ve ID ataması yapması lazım)
        restaurant.addProduct(product);

        // 4. Güncel Restoranı Kaydet (Aggregate Root kaydedilince child'lar da kaydedilir)
        restaurantRepository.saveRestaurant(restaurant);

        log.info("Product is added with id: {} to restaurant: {}", product.getId().getValue(), restaurant.getId().getValue());

        return restaurantDataMapper.productToAddProductResponse(product);
    }
}
