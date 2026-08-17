package com.berkay.payment.service.messaging.listener.kafka;

import com.berkay.kafka.consumer.KafkaConsumer;
import com.berkay.kafka.order.avro.model.CustomerAvroModel;
import com.berkay.payment.service.domain.entity.CustomerWallet;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import com.berkay.payment.service.domain.valueobject.WalletId;
import com.berkay.domain.valueobject.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class CustomerKafkaListener implements KafkaConsumer<CustomerAvroModel> {

    private final WalletRepository walletRepository;

    public CustomerKafkaListener(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    @KafkaListener(id = "${kafka-consumer-config.customer-group-id}",
                   topics = "${payment-service.customer-topic-name}")
    public void receive(@Payload List<CustomerAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of customer create messages received", messages.size());

        messages.forEach(avroModel -> {
            UUID customerId = UUID.fromString(avroModel.getId());
            if (walletRepository.findByOwnerId(customerId).isEmpty()) {
                CustomerWallet customerWallet = new CustomerWallet(
                        new WalletId(UUID.randomUUID()),
                        customerId,
                        new Money(BigDecimal.ZERO)
                );
                walletRepository.save(customerWallet);
                log.info("Wallet successfully created for Customer with id: {}", customerId);
            }
        });
    }
}
