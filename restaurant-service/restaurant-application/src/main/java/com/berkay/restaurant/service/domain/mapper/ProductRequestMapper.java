package com.berkay.restaurant.service.domain.mapper;

import com.berkay.restaurant.service.domain.dto.create.AddProductRequest;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.update.UpdateProductRequest;
import com.berkay.restaurant.service.domain.dto.update.product.UpdateProductCommand;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductRequestMapper {

    public AddProductCommand addProductRequestToAddProductCommand(UUID restaurantId, AddProductRequest addProductRequest) {
        return AddProductCommand.builder()
                .restaurantId(restaurantId)
                .name(addProductRequest.getName())
                .price(addProductRequest.getPrice())
                .stock(addProductRequest.getStock())
                .available(addProductRequest.getAvailable())
                .build();
    }

    public UpdateProductCommand updateProductRequestToUpdateProductCommand(UUID restaurantId,
                                                                           UUID productId,
                                                                           UpdateProductRequest updateProductRequest) {
        return UpdateProductCommand.builder()
                .restaurantId(restaurantId)
                .productId(productId)
                .name(updateProductRequest.getName())
                .price(updateProductRequest.getPrice())
                .stock(updateProductRequest.getStock())
                .available(updateProductRequest.getAvailable())
                .build();
    }
}

