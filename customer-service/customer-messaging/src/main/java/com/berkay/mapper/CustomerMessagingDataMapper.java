package com.berkay.mapper;

import com.berkay.customer.service.outbox.model.CustomerEventPayload;
import com.berkay.kafka.order.avro.model.CustomerAvroModel;
import org.springframework.stereotype.Component;

@Component
public class CustomerMessagingDataMapper {
// Kafka tarafındaki mapper, eskiden CustomerCreatedEvent alıyordu.
// Artık JSON'dan çevirdiğimiz CustomerEventPayload nesnesini alıp Avro modeline çevirecek
    public CustomerAvroModel customerEventPayloadToCustomerAvroModel(CustomerEventPayload
                                                                             customerEventPayload) {
        return CustomerAvroModel.newBuilder()
                .setId(customerEventPayload.getCustomerId())
                .setUsername(customerEventPayload.getUsername())
                .setFirstName(customerEventPayload.getFirstName())
                .setLastName(customerEventPayload.getLastName())
                .setEmail(customerEventPayload.getEmail())
                .build();
    }
}
