package com.berkay.payment.service.messaging.listener.kafka;

import com.berkay.kafka.consumer.KafkaConsumer;
import com.berkay.kafka.order.avro.model.OrderDeliveredAvroModel;
import com.berkay.payment.service.domain.ports.input.message.listener.OrderDeliveredMessageListener;
import com.berkay.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderDeliveredKafkaListener implements KafkaConsumer<OrderDeliveredAvroModel> {

    private final OrderDeliveredMessageListener orderDeliveredMessageListener;
    private final PaymentMessagingDataMapper paymentMessagingDataMapper;

    public OrderDeliveredKafkaListener(OrderDeliveredMessageListener orderDeliveredMessageListener,
                                       PaymentMessagingDataMapper paymentMessagingDataMapper) {
        this.orderDeliveredMessageListener = orderDeliveredMessageListener;
        this.paymentMessagingDataMapper = paymentMessagingDataMapper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.order-delivered-consumer-group-id:payment-order-delivered-topic-consumer}",
                   topics = "${payment-service.order-delivered-topic-name:order-delivered}")
    public void receive(@Payload List<OrderDeliveredAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of order delivered messages received with keys: {}, partitions: {}, offsets: {}",
                messages.size(), keys, partitions, offsets);

        messages.forEach(avroModel -> {
            try {
                orderDeliveredMessageListener.settleOrder(
                        paymentMessagingDataMapper.orderDeliveredAvroModelToOrderDeliveredRequest(avroModel));
            } catch (Exception e) {
                log.error("Error processing settlement for order delivered event: {}", avroModel.getOrderId(), e);
            }
        });
    }
}
