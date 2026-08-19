package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.read.GetProductListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetProductQuery;
import com.berkay.restaurant.service.domain.dto.read.GetProductQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicProductListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicProductQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQuery;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQueryResponse;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.ProductNotFoundException;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestaurantQueryHandler {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantDataMapper restaurantDataMapper;

    public RestaurantQueryHandler(RestaurantRepository restaurantRepository,
                                  RestaurantDataMapper restaurantDataMapper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantDataMapper = restaurantDataMapper;
    }

    @Transactional(readOnly = true)
    public GetRestaurantQueryResponse getRestaurant(GetRestaurantQuery query) {
        Restaurant restaurant = findRestaurantById(query.getRestaurantId());
        return restaurantDataMapper.restaurantToGetRestaurantQueryResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public GetProductQueryResponse getProduct(GetProductQuery query) {
        Restaurant restaurant = findRestaurantById(query.getRestaurantId());

        Optional<Product> productResult = restaurant.getMenu().stream()
                .filter(product -> product.getId().getValue().equals(query.getProductId()))
                .findFirst();

        if (productResult.isEmpty()) {
            log.warn("Product with id: {} not found in restaurant: {}", query.getProductId(), query.getRestaurantId());
            throw new ProductNotFoundException("Product not found with id: " + query.getProductId());
        }

        return restaurantDataMapper.productToGetProductQueryResponse(productResult.get());
    }

    @Transactional(readOnly = true)
    public GetProductListQueryResponse getProducts(UUID restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        List<GetProductQueryResponse> products = restaurant.getMenu().stream()
                .map(restaurantDataMapper::productToGetProductQueryResponse)
                .collect(Collectors.toList());
        return new GetProductListQueryResponse(restaurantId, products);
    }

    @Transactional(readOnly = true)
    public GetPublicProductListQueryResponse getPublicProducts(UUID restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        List<GetPublicProductQueryResponse> products = restaurant.getMenu().stream()
                .filter(product -> !product.isHidden())
                .map(restaurantDataMapper::productToGetPublicProductQueryResponse)
                .collect(Collectors.toList());
        return new GetPublicProductListQueryResponse(restaurantId, products);
    }

    @Transactional(readOnly = true)
    public GetPublicProductQueryResponse getPublicProduct(UUID restaurantId, UUID productId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        
        Optional<Product> productResult = restaurant.getMenu().stream()
                .filter(product -> product.getId().getValue().equals(productId) && !product.isHidden())
                .findFirst();

        if (productResult.isEmpty()) {
            log.warn("Public product with id: {} not found in restaurant: {}", productId, restaurantId);
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }

        return restaurantDataMapper.productToGetPublicProductQueryResponse(productResult.get());
    }

    private Restaurant findRestaurantById(UUID restaurantId) {
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantById(restaurantId);
        if (restaurantResult.isEmpty()) {
            log.error("Restaurant with id: {} not found!", restaurantId);
            throw new RestaurantNotFoundException("Restaurant not found with id: " + restaurantId);
        }
        return restaurantResult.get();
    }
}
