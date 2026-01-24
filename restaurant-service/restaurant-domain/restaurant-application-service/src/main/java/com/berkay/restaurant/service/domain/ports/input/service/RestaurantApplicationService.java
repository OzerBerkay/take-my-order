package com.berkay.restaurant.service.domain.ports.input.service;

import com.berkay.restaurant.service.domain.dto.create.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.dto.update.UpdateProductCommand;
import com.berkay.restaurant.service.domain.dto.update.UpdateRestaurantCommand;
import jakarta.validation.Valid;

public interface RestaurantApplicationService {
    CreateRestaurantResponse createRestaurant(@Valid CreateRestaurantCommand createRestaurantCommand);

    AddProductResponse addProduct(@Valid AddProductCommand addProductCommand);

    void updateRestaurant(UpdateRestaurantCommand updateRestaurantCommand);

    void updateProduct(UpdateProductCommand updateProductCommand);
}
