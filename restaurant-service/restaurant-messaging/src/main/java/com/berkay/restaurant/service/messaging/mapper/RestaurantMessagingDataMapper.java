package com.berkay.restaurant.service.messaging.mapper;

import com.berkay.domain.valueobject.RestaurantOrderStatus;
import com.berkay.kafka.order.avro.model.OrderApprovalStatus;
import com.berkay.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.berkay.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.berkay.kafka.order.avro.model.RestaurantCreatedAvroModel;
import com.berkay.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.berkay.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantEventPayload;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantMessagingDataMapper {

    public RestaurantApprovalRequest
    restaurantApprovalRequestAvroModelToRestaurantApproval (RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel) {
        return RestaurantApprovalRequest.builder()
                .id(restaurantApprovalRequestAvroModel.getId())
                .sagaId(restaurantApprovalRequestAvroModel.getSagaId())
                .restaurantId(restaurantApprovalRequestAvroModel.getRestaurantId())
                .orderId(restaurantApprovalRequestAvroModel.getOrderId())
                .restaurantOrderStatus(RestaurantOrderStatus.valueOf(restaurantApprovalRequestAvroModel
                        .getRestaurantOrderStatus().name()))
                .productQuantities(restaurantApprovalRequestAvroModel.getProductQuantities().stream()
                        .map(avroModel ->
                                RestaurantApprovalRequest.ProductQuantity.builder()
                                        .id(avroModel.getId().toString())
                                        .quantity(avroModel.getQuantity())
                                        .build())
                        .collect(Collectors.toList()))
                .price(restaurantApprovalRequestAvroModel.getPrice())
                .createdAt(restaurantApprovalRequestAvroModel.getCreatedAt())
                .build();
    }

    public RestaurantApprovalResponseAvroModel
    orderEventPayloadToRestaurantApprovalResponseAvroModel(String sagaId, OrderEventPayload orderEventPayload) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId(sagaId)
                .setOrderId(orderEventPayload.getOrderId())
                .setRestaurantId(orderEventPayload.getRestaurantId())
                .setCreatedAt(orderEventPayload.getCreatedAt().toInstant())
                .setOrderApprovalStatus(OrderApprovalStatus.valueOf(orderEventPayload.getOrderApprovalStatus()))
                .setFailureMessages(orderEventPayload.getFailureMessages())
                .build();
    }

    public RestaurantCreatedAvroModel restaurantEventPayloadToRestaurantCreatedAvroModel(RestaurantEventPayload restaurantEventPayload) {
        return RestaurantCreatedAvroModel.newBuilder()
                .setRestaurantId(java.util.UUID.fromString(restaurantEventPayload.getRestaurantId()))
                .setActive(restaurantEventPayload.isActive())
                .setCreatedAt(restaurantEventPayload.getCreatedAt().toInstant())
                .setProducts(restaurantEventPayload.getProducts().stream().map(productPayload ->
                        com.berkay.kafka.order.avro.model.RestaurantProduct.newBuilder()
                                .setProductId(java.util.UUID.fromString(productPayload.getProductId()))
                                .setName(productPayload.getName())
                                .setPrice(productPayload.getPrice())
                                .setAvailable(productPayload.isAvailable())
                                .build()).collect(java.util.stream.Collectors.toList()))
                .build();
    }

}
