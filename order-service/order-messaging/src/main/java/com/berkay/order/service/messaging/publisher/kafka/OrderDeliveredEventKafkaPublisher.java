package com.berkay.order.service.messaging.publisher.kafka;

import com.berkay.kafka.order.avro.model.OrderDeliveredAvroModel;
import com.berkay.kafka.producer.KafkaMessageHelper;
import com.berkay.kafka.producer.service.KafkaProducer;
import com.berkay.order.service.domain.config.OrderServiceConfigData;
import com.berkay.order.service.domain.outbox.model.delivery.DeliveryOutboxMessage;
import com.berkay.order.service.domain.outbox.model.delivery.OrderDeliveredEventPayload;
import com.berkay.order.service.domain.ports.output.message.publisher.delivery.DeliveryMessagePublisher;
import com.berkay.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class OrderDeliveredEventKafkaPublisher implements DeliveryMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final KafkaProducer<String, OrderDeliveredAvroModel> kafkaProducer;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    public OrderDeliveredEventKafkaPublisher(OrderMessagingDataMapper orderMessagingDataMapper,
                                             KafkaProducer<String, OrderDeliveredAvroModel> kafkaProducer,
                                             OrderServiceConfigData orderServiceConfigData,
                                             KafkaMessageHelper kafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(DeliveryOutboxMessage deliveryOutboxMessage,
                        BiConsumer<DeliveryOutboxMessage, OutboxStatus> outboxCallback) {
        OrderDeliveredEventPayload payload =
                kafkaMessageHelper.getEventPayload(deliveryOutboxMessage.getPayload(), OrderDeliveredEventPayload.class);

        String orderId = payload.getOrderId().toString();
        log.info("Received DeliveryOutboxMessage for order id: {}", orderId);

        try {
            OrderDeliveredAvroModel avroModel = orderMessagingDataMapper
                    .orderDeliveredEventToOrderDeliveredAvroModel(payload);

            kafkaProducer.send(
                    orderServiceConfigData.getOrderDeliveredTopicName(),
                    orderId,
                    avroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getOrderDeliveredTopicName(),
                            avroModel,
                            deliveryOutboxMessage,
                            outboxCallback,
                            orderId,
                            "OrderDeliveredAvroModel"));

            log.info("OrderDeliveredAvroModel sent to Kafka for order id: {}", orderId);
        } catch (Exception e) {
            log.error("Error while sending OrderDeliveredAvroModel to Kafka for order id: {}, error: {}",
                    orderId, e.getMessage());
        }
    }
}
