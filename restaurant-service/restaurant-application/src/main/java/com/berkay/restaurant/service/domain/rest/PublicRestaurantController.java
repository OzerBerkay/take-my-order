package com.berkay.restaurant.service.domain.rest;

import com.berkay.restaurant.service.domain.dto.read.GetPublicProductListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicProductQueryResponse;
import com.berkay.restaurant.service.domain.ports.input.service.RestaurantApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/public/restaurants", produces = "application/vnd.api.v1+json")
public class PublicRestaurantController {

    private final RestaurantApplicationService restaurantApplicationService;

    public PublicRestaurantController(RestaurantApplicationService restaurantApplicationService) {
        this.restaurantApplicationService = restaurantApplicationService;
    }

    @GetMapping
    public ResponseEntity<com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQueryResponse> getPublicRestaurants(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String searchName,
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.util.List<String> cuisineCodes,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean available,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting public restaurants with searchName: {}, cuisineCodes: {}, available: {}, page: {}, size: {}", searchName, cuisineCodes, available, page, size);
        com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQuery query = 
                new com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQuery(searchName, cuisineCodes, available, page, size);
        
        com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantListQueryResponse response = 
                restaurantApplicationService.getPublicRestaurants(query);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantQueryResponse> getPublicRestaurant(@PathVariable UUID restaurantId) {
        log.info("Getting public restaurant details for id: {}", restaurantId);

        com.berkay.restaurant.service.domain.dto.read.GetPublicRestaurantQueryResponse response = 
                restaurantApplicationService.getPublicRestaurant(restaurantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{restaurantId}/products")
    public ResponseEntity<GetPublicProductListQueryResponse> getPublicProducts(@PathVariable UUID restaurantId) {
        log.info("Getting all public products from restaurant: {}", restaurantId);

        GetPublicProductListQueryResponse response = restaurantApplicationService.getPublicProducts(restaurantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{restaurantId}/products/{productId}")
    public ResponseEntity<GetPublicProductQueryResponse> getPublicProduct(@PathVariable UUID restaurantId,
                                                                          @PathVariable UUID productId) {
        log.info("Getting public product with id: {} from restaurant: {}", productId, restaurantId);

        GetPublicProductQueryResponse response = restaurantApplicationService.getPublicProduct(restaurantId, productId);
        return ResponseEntity.ok(response);
    }
}
