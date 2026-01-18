package com.berkay.order.service.domain;

import com.berkay.order.service.domain.dto.create.CreateOrderCommand;
import com.berkay.order.service.domain.dto.create.OrderItem;
import com.berkay.order.service.domain.entity.Customer;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.entity.Product;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.order.service.domain.event.OrderCreatedEvent;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.CustomerRepository;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderCreateHelper {

    private final OrderDomainService orderDomainService;

    private final OrderRepository orderRepository;

    private final CustomerRepository customerRepository;

    private final RestaurantRepository restaurantRepository;

    private final OrderDataMapper orderDataMapper;

    // this is a constructor injection
    public OrderCreateHelper(OrderDomainService orderDomainService,
                             OrderRepository orderRepository,
                             CustomerRepository customerRepository,
                             RestaurantRepository restaurantRepository,
                             OrderDataMapper orderDataMapper) {
        this.orderDomainService = orderDomainService;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderDataMapper = orderDataMapper;
    }

    @Transactional
    public OrderCreatedEvent persistOrder(CreateOrderCommand createOrderCommand) {
        // Müşteri var mı kontrolü
        checkCustomer(createOrderCommand.getCustomerId());

        // restaurant ve product kontrolü
        Restaurant restaurant = checkRestaurantAndProduct(createOrderCommand);

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order, restaurant);
        Order orderResult = saveOrder(order);
        return orderCreatedEvent;
    }

    private Restaurant checkRestaurantAndProduct(CreateOrderCommand createOrderCommand) {
        // DTO'dan Restoran ID'sini al
        UUID restaurantId = createOrderCommand.getRestaurantId();

        // DTO'dan Ürün ID'lerini topla
        Set<UUID> requestedProductIds = createOrderCommand.getItems().stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toSet());

        // Repository'i çağır ve Restoranı yalnızca ilgili product'larıyla birlikte bul
        Optional<Restaurant> optionalRestaurant =
                restaurantRepository.findRestaurantWithProducts(restaurantId, new ArrayList<>(requestedProductIds));

        // Restorant gerçekten var mı?
        if (optionalRestaurant.isEmpty()) {
            log.warn("Could not find restaurant with id: {}", createOrderCommand.getRestaurantId());
            throw new OrderDomainException("Could not find restaurant with id: " + createOrderCommand.getRestaurantId());
        }

        Restaurant restaurant = optionalRestaurant.get();

        // DB'den gelen ürünleri (ID -> Product) Map'e çevirelim, erişim kolay olsun.
        Map<UUID, Product> dbProductsMap = restaurant.getProducts().stream()
                .collect(Collectors.toMap(p -> p.getId().getValue(), p -> p));

        // Ürün restorantta gerçekten var mı?
        List<String> validationErrors = new ArrayList<>();
        for (UUID requestedId : requestedProductIds) {
            if (!dbProductsMap.containsKey(requestedId)) {
                // Ürün veritabanında hiç yok (ID yanlış veya bu restorana ait değil)
                validationErrors.add("Product with id: " + requestedId + " not found in restaurant");
            }
        }

        // Hata varsa fırlat (Toplu raporlama)
        if (!validationErrors.isEmpty()) {
            log.warn("Order validation failed for restaurant {}: {}", restaurantId, validationErrors);
            throw new OrderDomainException("Validation failed: " + String.join(", ", validationErrors));
        }

        return restaurant;
    }

    private void checkCustomer(UUID customerId) {
        Optional<Customer> customer = customerRepository.findCustomer(customerId);
        if (customer.isEmpty()) {
            log.warn("Could not find customer with id: {}", customerId);
            throw new OrderDomainException("Could not find customer with id: " + customerId);
        }
    }

    private Order saveOrder(Order order) {
        Order orderResult = orderRepository.save(order);
        if (orderResult == null) {
            log.error("Order could not be saved!");
            throw new OrderDomainException("Order could not be saved!");
        }
        log.info("Order is created with id: {}", orderResult.getId().getValue());
        return orderResult;
    }


}
