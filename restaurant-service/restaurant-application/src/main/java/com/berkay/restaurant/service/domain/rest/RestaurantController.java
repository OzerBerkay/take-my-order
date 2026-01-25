package com.berkay.restaurant.service.domain.rest;

import com.berkay.restaurant.service.domain.dto.create.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.dto.update.UpdateProductCommand;
import com.berkay.restaurant.service.domain.dto.update.UpdateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.update.UpdateRestaurantRequest;
import com.berkay.restaurant.service.domain.mapper.RestaurantRequestMapper;
import com.berkay.restaurant.service.domain.ports.input.service.RestaurantApplicationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/restaurants", produces = "application/vnd.api.v1+json")
public class RestaurantController {

    private final RestaurantApplicationService restaurantApplicationService;
    private final RestaurantRequestMapper restaurantRequestMapper;

    public RestaurantController(RestaurantApplicationService restaurantApplicationService,
                                RestaurantRequestMapper restaurantRequestMapper) {

        this.restaurantApplicationService = restaurantApplicationService;
        this.restaurantRequestMapper = restaurantRequestMapper;
    }

    @PostMapping
    public ResponseEntity<CreateRestaurantResponse> createRestaurant(@RequestBody CreateRestaurantCommand createRestaurantCommand) {
        log.info("Creating restaurant with name: {}", createRestaurantCommand.getRestaurantName());
        CreateRestaurantResponse response = restaurantApplicationService.createRestaurant(createRestaurantCommand);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products")
    public ResponseEntity<AddProductResponse> addProduct(@RequestBody AddProductCommand addProductCommand) {
        log.info("Adding product to restaurant with id: {}", addProductCommand.getRestaurantId());

        AddProductResponse response = restaurantApplicationService.addProduct(addProductCommand);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{restaurantId}")
    public ResponseEntity<String> updateRestaurant(@PathVariable UUID restaurantId,
                                                   @RequestBody @Valid UpdateRestaurantRequest updateRestaurantRequest) {
        log.info("Updating restaurant with id: {}", restaurantId);

        UpdateRestaurantCommand updateRestaurantCommand = restaurantRequestMapper
                .updateRestaurantRequestToCommand(restaurantId, updateRestaurantRequest);

        restaurantApplicationService.updateRestaurant(updateRestaurantCommand);
        return ResponseEntity.ok("Restaurant updated");
    }

    // Ürün Güncelleme (Fiyat/Stok/Durum)
    @PutMapping("/products")
    public ResponseEntity<String> updateProduct(@RequestBody UpdateProductCommand updateProductCommand) {
        log.info("Updating product with id: {} for restaurant id: {}",
                updateProductCommand.getProductId(), updateProductCommand.getRestaurantId());

        restaurantApplicationService.updateProduct(updateProductCommand);
        return ResponseEntity.ok("Product updated");
    }
}