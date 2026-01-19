package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.domain.valueobject.ProductId;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.berkay.restaurant.service.domain.entity.OrderDetail;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.OrderApprovalEvent;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.berkay.restaurant.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
import com.berkay.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
public class RestaurantApprovalRequestHelper {

    private final RestaurantDomainService restaurantDomainService;
    private final RestaurantDataMapper restaurantDataMapper;
    private final RestaurantRepository restaurantRepository;
    private final OrderApprovalRepository orderApprovalRepository;
    private final OrderOutboxHelper orderOutboxHelper;
    private final RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher;



    public RestaurantApprovalRequestHelper(RestaurantDomainService restaurantDomainService,
                                           RestaurantDataMapper restaurantDataMapper,
                                           RestaurantRepository restaurantRepository,
                                           OrderApprovalRepository orderApprovalRepository,
                                           OrderOutboxHelper orderOutboxHelper,
                                           RestaurantApprovalResponseMessagePublisher
                                                   restaurantApprovalResponseMessagePublisher) {
        this.restaurantDomainService = restaurantDomainService;
        this.restaurantDataMapper = restaurantDataMapper;
        this.restaurantRepository = restaurantRepository;
        this.orderApprovalRepository = orderApprovalRepository;
        this.orderOutboxHelper = orderOutboxHelper;
        this.restaurantApprovalResponseMessagePublisher = restaurantApprovalResponseMessagePublisher;
    }

    @Transactional
    public void persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        if (publishIfOutboxMessageProcessed(restaurantApprovalRequest)) {
            log.info("An outbox message with saga id: {} already saved to database!",
                    restaurantApprovalRequest.getSagaId());
            return;
        }

        log.info("Processing restaurant approval for order id: {}", restaurantApprovalRequest.getOrderId());
        List<String> failureMessages = new ArrayList<>();

        // Restoranı ve Menüsünü Bul
        Restaurant restaurant = findRestaurant(restaurantApprovalRequest);

        // Validasyon Yap (Domain Core)
        OrderApprovalEvent orderApprovalEvent =
                restaurantDomainService.validateOrder(
                        restaurant,
                        failureMessages);

        // Sonucu Kaydet
        orderApprovalRepository.save(restaurant.getOrderApproval());

        orderOutboxHelper
                .saveOrderOutboxMessage(restaurantDataMapper.orderApprovalEventToOrderEventPayload(orderApprovalEvent),
                        orderApprovalEvent.getOrderApproval().getApprovalStatus(),
                        OutboxStatus.STARTED,
                        UUID.fromString(restaurantApprovalRequest.getSagaId()));

    }

    private Restaurant findRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
        // Restoranı Bul
        // TODO: Restorandan sadece siparişe özel ürünler gelmeli tüm menü değil
        UUID restaurantId = UUID.fromString(restaurantApprovalRequest.getRestaurantId());
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantById(restaurantId);

        // Restoranın varlığını kontrol et
        if (restaurantResult.isEmpty()) {
            log.error("Restaurant with id " + restaurantId + " not found!");
            // TODO: Kafka listener'da hatayı yutmak yerine rejected status ile event fırlatılmalı
            throw new RestaurantNotFoundException("Restaurant with id " + restaurantId + " not found!");
        }

        // Restoranı optional'den al (id, name, isActive, menu)
        Restaurant restaurantEntity = restaurantResult.get();

        // Request'ten gelen ürün miktarlarını Map'e çeviriyoruz
        Map<ProductId, Integer> quantities = new HashMap<>();
        restaurantApprovalRequest.getProductQuantities().forEach(requestedProduct -> {
            quantities.put(
                    new ProductId(UUID.fromString(requestedProduct.getId())),
                    requestedProduct.getQuantity()
            );
        });

        // Restoranın tam olması için OrderDetail eklenmesi gerek
        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())))
                .totalAmount(new Money(restaurantApprovalRequest.getPrice()))
                .orderStatus(OrderStatus.valueOf(restaurantApprovalRequest.getRestaurantOrderStatus().name()))
                .productQuantities(quantities)
                .build();
        restaurantEntity.initOrderDetail(orderDetail);

        return restaurantEntity;
    }

    private boolean publishIfOutboxMessageProcessed(RestaurantApprovalRequest restaurantApprovalRequest) {
        Optional<OrderOutboxMessage> orderOutboxMessage =
                orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndOutboxStatus(UUID
                        .fromString(restaurantApprovalRequest.getSagaId()), OutboxStatus.COMPLETED);
        if (orderOutboxMessage.isPresent()) {
            restaurantApprovalResponseMessagePublisher.publish(orderOutboxMessage.get(),
                    orderOutboxHelper::updateOutboxStatus);
            return true;
        }
        return false;
    }
}