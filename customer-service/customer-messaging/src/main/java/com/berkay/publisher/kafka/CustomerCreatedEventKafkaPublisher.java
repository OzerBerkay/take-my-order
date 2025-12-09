package com.berkay.publisher.kafka;

import com.berkay.customer.service.config.CustomerServiceConfigData;
import com.berkay.customer.service.outbox.model.CustomerEventPayload;
import com.berkay.customer.service.outbox.model.CustomerOutboxMessage;
import com.berkay.customer.service.ports.output.message.publisher.CustomerMessagePublisher;
import com.berkay.kafka.order.avro.model.CustomerAvroModel;
import com.berkay.kafka.producer.KafkaMessageHelper;
import com.berkay.kafka.producer.service.KafkaProducer;
import com.berkay.mapper.CustomerMessagingDataMapper;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class CustomerCreatedEventKafkaPublisher implements CustomerMessagePublisher {

    private final CustomerMessagingDataMapper customerMessagingDataMapper;
    private final KafkaProducer<String, CustomerAvroModel> kafkaProducer;
    private final CustomerServiceConfigData customerServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper; // YENİ OYUNCU

    public CustomerCreatedEventKafkaPublisher(CustomerMessagingDataMapper customerMessagingDataMapper,
                                              KafkaProducer<String, CustomerAvroModel> kafkaProducer,
                                              CustomerServiceConfigData customerServiceConfigData,
                                              KafkaMessageHelper kafkaMessageHelper) {
        this.customerMessagingDataMapper = customerMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.customerServiceConfigData = customerServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(CustomerOutboxMessage customerOutboxMessage,
                        BiConsumer<CustomerOutboxMessage, OutboxStatus> outboxCallback) {

        // 1. JSON Payload'ı Helper ile Çevir
        CustomerEventPayload customerEventPayload = kafkaMessageHelper
                .getEventPayload(customerOutboxMessage.getPayload(), CustomerEventPayload.class);

        log.info("Received CustomerOutboxMessage for customer id: {}", customerEventPayload.getCustomerId());

        try {
            // 2. Payload -> Avro Dönüşümü
            CustomerAvroModel customerAvroModel = customerMessagingDataMapper
                    .customerEventPayloadToCustomerAvroModel(customerEventPayload);

            // 3. Kafka'ya Gönder (Helper Callback ile)
            kafkaProducer.send(
                    customerServiceConfigData.getCustomerTopicName(),
                    customerAvroModel.getId(),
                    customerAvroModel,
                    // Helper bizim yerimize CompletableFuture/ListenableFuture karmaşasını yönetiyor
                    kafkaMessageHelper.getKafkaCallback(
                            customerServiceConfigData.getCustomerTopicName(),
                            customerAvroModel,
                            customerOutboxMessage,
                            outboxCallback,
                            customerEventPayload.getCustomerId(),
                            "CustomerAvroModel"
                    )
            );

            log.info("CustomerAvroModel sent to kafka for customer id: {}", customerAvroModel.getId());
        } catch (Exception e) {
            log.error("Error while sending CustomerAvroModel message" +
                            " to kafka with customer id: {}, error: {}",
                    customerEventPayload.getCustomerId(), e.getMessage());
            // Exception durumunda da helper'ın veya bizim manuel müdahalemiz gerekebilir
            // Ama genelde send metodu içindeki callback hatayı yakalar.
            // Yine de buraya safety-net olarak callback failed eklenebilir
        }
    }
}