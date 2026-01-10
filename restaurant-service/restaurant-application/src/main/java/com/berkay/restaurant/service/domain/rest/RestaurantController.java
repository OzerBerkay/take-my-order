package com.berkay.restaurant.service.domain.rest;

import com.berkay.restaurant.service.domain.AddProductCommandHandler;
import com.berkay.restaurant.service.domain.dto.create.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.CreateRestaurantCommandHandler; // Senin yazdığın Handler
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/restaurants", produces = "application/vnd.api.v1+json")
public class RestaurantController {

    private final CreateRestaurantCommandHandler createRestaurantCommandHandler;
    private final AddProductCommandHandler addProductCommandHandler;

    public RestaurantController(CreateRestaurantCommandHandler createRestaurantCommandHandler,
                                AddProductCommandHandler addProductCommandHandler) {
        this.createRestaurantCommandHandler = createRestaurantCommandHandler;
        this.addProductCommandHandler = addProductCommandHandler;
    }

    @PostMapping
    public ResponseEntity<CreateRestaurantResponse> createRestaurant(@RequestBody CreateRestaurantCommand createRestaurantCommand) {
        log.info("Creating restaurant with name: {}", createRestaurantCommand.getRestaurantName());
        CreateRestaurantResponse response = createRestaurantCommandHandler.createRestaurant(createRestaurantCommand);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products")
    public ResponseEntity<AddProductResponse> addProduct(@RequestBody AddProductCommand addProductCommand) {
        log.info("Adding product to restaurant with id: {}", addProductCommand.getRestaurantId());

        AddProductResponse response = addProductCommandHandler.addProduct(addProductCommand);

        return ResponseEntity.ok(response);
    }
}