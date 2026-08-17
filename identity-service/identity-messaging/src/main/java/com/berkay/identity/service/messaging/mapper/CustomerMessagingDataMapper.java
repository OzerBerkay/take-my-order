package com.berkay.identity.service.messaging.mapper;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.kafka.order.avro.model.CustomerAvroModel;
import org.springframework.stereotype.Component;

@Component
public class CustomerMessagingDataMapper {

    public CustomerAvroModel userToCustomerAvroModel(User user) {
        return CustomerAvroModel.newBuilder()
                .setId(user.getId().getValue().toString())
                .setUsername(user.getEmail().getValue())
                .setFirstName(user.getFirstName().getValue())
                .setLastName(user.getLastName().getValue())
                .setEmail(user.getEmail().getValue())
                .build();
    }
}
