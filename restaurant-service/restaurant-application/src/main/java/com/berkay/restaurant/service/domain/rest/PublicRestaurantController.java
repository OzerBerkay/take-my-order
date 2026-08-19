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
