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

    com.berkay.restaurant.service.domain.dto.create.product.AddProductBatchResponse addProductBatch(@Valid com.berkay.restaurant.service.domain.dto.create.product.AddProductBatchCommand addProductBatchCommand);

    void updateRestaurant(UpdateRestaurantCommand updateRestaurantCommand);

    com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateCategoriesResponse updateCategories(@Valid com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateCategoriesCommand updateCategoriesCommand);

    void updateProduct(UpdateProductCommand updateProductCommand);

    void deleteProduct(DeleteProductCommand deleteProductCommand);

    GetRestaurantQueryResponse getRestaurant(GetRestaurantQuery getRestaurantQuery);

    com.berkay.restaurant.service.domain.dto.read.GetRestaurantListQueryResponse getRestaurants(java.util.UUID userId);

    GetProductQueryResponse getProduct(GetProductQuery getProductQuery);

    GetProductListQueryResponse getProducts(java.util.UUID restaurantId);

    GetPublicProductListQueryResponse getPublicProducts(java.util.UUID restaurantId);

    GetPublicProductQueryResponse getPublicProduct(java.util.UUID restaurantId, java.util.UUID productId);
    
    com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQueryResponse getPublicRestaurants(com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQuery getPublicRestaurantListQuery);
    
    com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantQueryResponse getPublicRestaurant(java.util.UUID restaurantId);

    com.berkay.restaurant.service.domain.dto.read.GetRestaurantCategoriesResponse getRestaurantCategories(java.util.UUID restaurantId);
}
