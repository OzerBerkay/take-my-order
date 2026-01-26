package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.delete.DeleteProductCommand;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.exception.ProductNotFoundException;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.scheduler.RestaurantOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxEventType.RESTAURANT_UPDATED;

@Slf4j
@Component
public class DeleteProductCommandHandler {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantOutboxHelper restaurantOutboxHelper;
    private final RestaurantDataMapper restaurantDataMapper;

    public DeleteProductCommandHandler(RestaurantRepository restaurantRepository,
                                       RestaurantOutboxHelper restaurantOutboxHelper,
                                       RestaurantDataMapper restaurantDataMapper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantOutboxHelper = restaurantOutboxHelper;
        this.restaurantDataMapper = restaurantDataMapper;
    }

    @Transactional
    public void deleteProduct(DeleteProductCommand command) {
        UUID restaurantId = command.getRestaurantId();
        UUID productId = command.getProductId();

        // Restoranı Bul
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantById(restaurantId);
        if (restaurantResult.isEmpty()) {
            log.error("Restaurant not found with id: {}", restaurantId);
            throw new RestaurantNotFoundException("Restaurant not found: " + restaurantId);
        }
        Restaurant restaurant = restaurantResult.get();

        // Ürünü Bul (Silmeden önce var mı diye bakıyoruz, yoksa hata fırlatacağız)
        Optional<Product> productResult = restaurant.getMenu().stream()
                .filter(p -> p.getId().getValue().equals(productId))
                .findFirst();

        if (productResult.isEmpty()) {
            log.warn("Product with id: {} not found inside restaurant: {}", productId, restaurantId);
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }

        // Ürünü Listeden Sil (Domain Logic)
        // Burada objeyi listeden uçuruyoruz. JPA'da orphanRemoval=true ise DB'den de uçar.
        restaurant.getMenu().remove(productResult.get());

        // Restoranı Kaydet (Güncel listeyi DB'ye yansıt)
        Restaurant savedRestaurant = restaurantRepository.saveRestaurant(restaurant);

        // Outbox'a Event At (Snapshot: Artık menüde o ürün yok, Consumer bunu böyle bilecek)
        restaurantOutboxHelper.saveRestaurantOutboxMessage(
                restaurantDataMapper.restaurantToRestaurantInformationEvent(savedRestaurant),
                RESTAURANT_UPDATED
        );

        log.info("Product with id: {} is deleted from restaurant: {}", productId, restaurantId);
    }
}