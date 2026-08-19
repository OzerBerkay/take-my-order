package com.berkay.restaurant.service.domain.ports.input.service;

import com.berkay.restaurant.service.domain.dto.create.product.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.dto.delete.DeleteProductCommand;
import com.berkay.restaurant.service.domain.dto.read.GetProductQuery;
import com.berkay.restaurant.service.domain.dto.read.GetProductQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQuery;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetProductListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicProductListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicProductQueryResponse;
import com.berkay.restaurant.service.domain.dto.update.product.UpdateProductCommand;
import com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateRestaurantCommand;
import jakarta.validation.Valid;

import java.util.UUID;

public interface RestaurantApplicationService {
    CreateRestaurantResponse createRestaurant(@Valid CreateRestaurantCommand createRestaurantCommand);

    AddProductResponse addProduct(@Valid AddProductCommand addProductCommand);

    void updateRestaurant(UpdateRestaurantCommand updateRestaurantCommand);

    void updateProduct(UpdateProductCommand updateProductCommand);

    void deleteProduct(DeleteProductCommand deleteProductCommand);

    GetRestaurantQueryResponse getRestaurant(GetRestaurantQuery getRestaurantQuery);

    GetProductQueryResponse getProduct(GetProductQuery getProductQuery);

    GetProductListQueryResponse getProducts(UUID restaurantId);

    GetPublicProductListQueryResponse getPublicProducts(UUID restaurantId);

    GetPublicProductQueryResponse getPublicProduct(UUID restaurantId, UUID productId);
}
