package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.create.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.dto.update.UpdateProductCommand;
import com.berkay.restaurant.service.domain.dto.update.UpdateRestaurantCommand;
import com.berkay.restaurant.service.domain.ports.input.service.RestaurantApplicationService;

public class RestaurantApplicationServiceImpl implements RestaurantApplicationService {

    private final CreateRestaurantCommandHandler createRestaurantCommandHandler;
    private final AddProductCommandHandler addProductCommandHandler;
    private final UpdateRestaurantCommandHandler updateRestaurantCommandHandler;
    private final UpdateProductCommandHandler updateProductCommandHandler;

    public RestaurantApplicationServiceImpl(CreateRestaurantCommandHandler createRestaurantCommandHandler,
                                            AddProductCommandHandler addProductCommandHandler,
                                            UpdateRestaurantCommandHandler updateRestaurantCommandHandler,
                                            UpdateProductCommandHandler updateProductCommandHandler) {
        this.createRestaurantCommandHandler = createRestaurantCommandHandler;
        this.addProductCommandHandler = addProductCommandHandler;
        this.updateRestaurantCommandHandler = updateRestaurantCommandHandler;
        this.updateProductCommandHandler = updateProductCommandHandler;
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
}
