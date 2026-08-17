package com.berkay.identity.service.messaging.publisher.kafka;

import com.berkay.identity.service.config.IdentityServiceConfigData;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.messaging.mapper.CustomerMessagingDataMapper;
import com.berkay.identity.service.ports.output.message.publisher.CustomerMessagePublisher;
import com.berkay.kafka.order.avro.model.CustomerAvroModel;
import com.berkay.kafka.producer.service.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventKafkaPublisher implements CustomerMessagePublisher {

    private final CustomerMessagingDataMapper customerMessagingDataMapper;
    private final KafkaProducer<String, CustomerAvroModel> kafkaProducer;
    private final IdentityServiceConfigData identityServiceConfigData;

    @Override
    public void publish(User user) {
        log.info("Received CustomerMessagePublisher publish event for user id: {}", user.getId().getValue());
        try {
            CustomerAvroModel customerAvroModel = customerMessagingDataMapper.userToCustomerAvroModel(user);
            
            kafkaProducer.send(identityServiceConfigData.getCustomerTopicName(),
                    user.getId().getValue().toString(),
                    customerAvroModel,
                    (result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent CustomerAvroModel to Kafka for user id: {}", customerAvroModel.getId());
                        } else {
                            log.error("Failed to send CustomerAvroModel to Kafka for user id: {}", customerAvroModel.getId(), ex);
                        }
                    });

            log.info("CustomerAvroModel sent to Kafka for user id: {}", customerAvroModel.getId());
        } catch (Exception e) {
            log.error("Error while sending CustomerAvroModel message to kafka with user id: {}", user.getId().getValue(), e);
        }
    }
}
