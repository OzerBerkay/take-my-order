package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.create.product.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.dto.delete.DeleteProductCommand;
import com.berkay.restaurant.service.domain.dto.read.GetProductListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetProductQuery;
import com.berkay.restaurant.service.domain.dto.read.GetProductQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicProductListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicProductQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQuery;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQueryResponse;
import com.berkay.restaurant.service.domain.dto.update.product.UpdateProductCommand;
import com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateRestaurantCommand;
import com.berkay.restaurant.service.domain.ports.input.service.RestaurantApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Slf4j
@Validated
@Service
public class RestaurantApplicationServiceImpl implements RestaurantApplicationService {

    private final CreateRestaurantCommandHandler createRestaurantCommandHandler;
    private final AddProductCommandHandler addProductCommandHandler;
    private final UpdateRestaurantCommandHandler updateRestaurantCommandHandler;
    private final UpdateProductCommandHandler updateProductCommandHandler;
    private final DeleteProductCommandHandler deleteProductCommandHandler;
    private final RestaurantQueryHandler restaurantQueryHandler;

    public RestaurantApplicationServiceImpl(CreateRestaurantCommandHandler createRestaurantCommandHandler,
                                            AddProductCommandHandler addProductCommandHandler,
                                            UpdateRestaurantCommandHandler updateRestaurantCommandHandler,
                                            UpdateProductCommandHandler updateProductCommandHandler,
                                            DeleteProductCommandHandler deleteProductCommandHandler,
                                            RestaurantQueryHandler restaurantQueryHandler) {
        this.createRestaurantCommandHandler = createRestaurantCommandHandler;
        this.addProductCommandHandler = addProductCommandHandler;
        this.updateRestaurantCommandHandler = updateRestaurantCommandHandler;
        this.updateProductCommandHandler = updateProductCommandHandler;
        this.deleteProductCommandHandler = deleteProductCommandHandler;
        this.restaurantQueryHandler = restaurantQueryHandler;
    }

    @Override
    public CreateRestaurantResponse createRestaurant(CreateRestaurantCommand createRestaurantCommand) {
        return createRestaurantCommandHandler.createRestaurant(createRestaurantCommand);
    }

    @Override
    public AddProductResponse addProduct(AddProductCommand addProductCommand) {
        return addProductCommandHandler.addProduct(addProductCommand);
    }

    @Override
    public void updateRestaurant(UpdateRestaurantCommand updateRestaurantCommand) {
        updateRestaurantCommandHandler.updateRestaurant(updateRestaurantCommand);
    }

    @Override
    public void updateProduct(UpdateProductCommand updateProductCommand) {
        updateProductCommandHandler.updateProduct(updateProductCommand);
    }

    @Override
    public void deleteProduct(DeleteProductCommand deleteProductCommand) {
        deleteProductCommandHandler.deleteProduct(deleteProductCommand);
    }

    @Override
    public GetRestaurantQueryResponse getRestaurant(GetRestaurantQuery getRestaurantQuery) {
        return restaurantQueryHandler.getRestaurant(getRestaurantQuery);
    }

    @Override
    public com.berkay.restaurant.service.domain.dto.read.GetRestaurantListQueryResponse getRestaurants(java.util.UUID userId) {
        return restaurantQueryHandler.getRestaurants(userId);
    }

    @Override
    public GetProductQueryResponse getProduct(GetProductQuery getProductQuery) {
        return restaurantQueryHandler.getProduct(getProductQuery);
    }

    @Override
    public GetProductListQueryResponse getProducts(java.util.UUID restaurantId) {
        return restaurantQueryHandler.getProducts(restaurantId);
    }

    @Override
    public GetPublicProductListQueryResponse getPublicProducts(java.util.UUID restaurantId) {
        return restaurantQueryHandler.getPublicProducts(restaurantId);
    }

    @Override
    public GetPublicProductQueryResponse getPublicProduct(java.util.UUID restaurantId, java.util.UUID productId) {
        return restaurantQueryHandler.getPublicProduct(restaurantId, productId);
    }

    @Override
    public com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQueryResponse getPublicRestaurants(com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQuery getPublicRestaurantListQuery) {
        return restaurantQueryHandler.getPublicRestaurants(getPublicRestaurantListQuery);
    }

    @Override
    public com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantQueryResponse getPublicRestaurant(java.util.UUID restaurantId) {
        return restaurantQueryHandler.getPublicRestaurant(restaurantId);
    }
}
