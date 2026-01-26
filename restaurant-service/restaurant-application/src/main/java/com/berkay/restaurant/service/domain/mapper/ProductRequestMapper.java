package com.berkay.restaurant.service.domain.mapper;

import com.berkay.restaurant.service.domain.dto.create.AddProductRequest;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductCommand;
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
}

