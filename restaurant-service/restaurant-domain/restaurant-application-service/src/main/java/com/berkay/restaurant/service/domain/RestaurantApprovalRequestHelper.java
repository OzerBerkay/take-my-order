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

import com.berkay.domain.valueobject.OrderApprovalStatus;
import com.berkay.domain.valueobject.RestaurantOrderStatus;
import com.berkay.restaurant.service.domain.event.OrderRejectedEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.berkay.domain.DomainConstants.UTC;

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
    public void processOrderApprovalRequest(RestaurantApprovalRequest restaurantApprovalRequest) {
        if (RestaurantOrderStatus.CANCELLED == restaurantApprovalRequest.getRestaurantOrderStatus()) {
            persistCancelOrderApproval(restaurantApprovalRequest);
        } else {
            persistOrderApproval(restaurantApprovalRequest);
        }
    }

    @Transactional
    public void persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        if (publishIfOutboxMessageProcessed(restaurantApprovalRequest, OrderApprovalStatus.VALIDATED)
                || publishIfOutboxMessageProcessed(restaurantApprovalRequest, OrderApprovalStatus.APPROVED)) {
            log.info("An outbox message with saga id: {} already saved to database!",
                    restaurantApprovalRequest.getSagaId());
            return;
        }

        log.info("Processing restaurant approval for order id: {}", restaurantApprovalRequest.getOrderId());

        // Restoranı ve Menüsünü Bul
        Restaurant restaurant = findRestaurant(restaurantApprovalRequest);

        // Validasyon Yap (Domain Core)
        Optional<OrderApprovalEvent> orderApprovalEventOpt =
                restaurantDomainService.validateOrder(restaurant);

        if (orderApprovalEventOpt.isPresent()) {
            OrderApprovalEvent orderApprovalEvent = orderApprovalEventOpt.get();
            orderApprovalRepository.save(restaurant.getOrderApproval());

            if (OrderApprovalStatus.VALIDATED == orderApprovalEvent.getOrderApproval().getApprovalStatus()
                    || OrderApprovalStatus.APPROVED == orderApprovalEvent.getOrderApproval().getApprovalStatus()) {
                restaurantRepository.saveRestaurant(restaurant);
            }

            orderOutboxHelper
                    .saveOrderOutboxMessage(restaurantDataMapper.orderApprovalEventToOrderEventPayload(orderApprovalEvent),
                            orderApprovalEvent.getOrderApproval().getApprovalStatus(),
                            OutboxStatus.STARTED,
                            UUID.fromString(restaurantApprovalRequest.getSagaId()));
        }
    }

    @Transactional
    public void persistCancelOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        if (publishIfOutboxMessageProcessed(restaurantApprovalRequest, OrderApprovalStatus.REJECTED)) {
            log.info("An outbox message with saga id: {} and status REJECTED already saved to database!",
                    restaurantApprovalRequest.getSagaId());
            return;
        }

        log.info("Processing stock compensation (restoreStock) for cancelled order id: {}", restaurantApprovalRequest.getOrderId());
        Restaurant restaurant = findRestaurant(restaurantApprovalRequest);
        restaurant.restoreStock(restaurant.getOrderDetail());

        restaurantRepository.saveRestaurant(restaurant);
        orderApprovalRepository.save(restaurant.getOrderApproval());

        OrderRejectedEvent orderRejectedEvent = new OrderRejectedEvent(
                restaurant.getOrderApproval(),
                restaurant.getId(),
                List.of("Order cancelled by chef: Stock successfully restored"),
                null,
                ZonedDateTime.now(ZoneId.of(UTC))
        );

        orderOutboxHelper.saveOrderOutboxMessage(
                restaurantDataMapper.orderApprovalEventToOrderEventPayload(orderRejectedEvent),
                OrderApprovalStatus.REJECTED,
                OutboxStatus.STARTED,
                UUID.fromString(restaurantApprovalRequest.getSagaId())
        );
        log.info("Stock restored and rejection event published for order id: {}", restaurantApprovalRequest.getOrderId());
    }

    private Restaurant findRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
        // Restoranı Bul
        UUID restaurantId = UUID.fromString(restaurantApprovalRequest.getRestaurantId());
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantByIdWithLock(restaurantId);

        // Restoranın varlığını kontrol et
        if (restaurantResult.isEmpty()) {
            log.error("Restaurant with id " + restaurantId + " not found!");
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

    private boolean publishIfOutboxMessageProcessed(RestaurantApprovalRequest restaurantApprovalRequest,
                                                    OrderApprovalStatus targetStatus) {
        Optional<OrderOutboxMessage> orderOutboxMessage =
                orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndApprovalStatusAndOutboxStatus(
                        UUID.fromString(restaurantApprovalRequest.getSagaId()), targetStatus, OutboxStatus.COMPLETED);
        if (orderOutboxMessage.isPresent()) {
            restaurantApprovalResponseMessagePublisher.publish(orderOutboxMessage.get(),
                    orderOutboxHelper::updateOutboxStatus);
            return true;
        }
        return false;
    }
}