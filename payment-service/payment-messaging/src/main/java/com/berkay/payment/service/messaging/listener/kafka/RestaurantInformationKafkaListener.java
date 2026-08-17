package com.berkay.payment.service.messaging.listener.kafka;

import com.berkay.kafka.consumer.KafkaConsumer;
import com.berkay.kafka.order.avro.model.RestaurantInformationAvroModel;
import com.berkay.payment.service.domain.entity.RestaurantWallet;
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
public class RestaurantInformationKafkaListener implements KafkaConsumer<RestaurantInformationAvroModel> {

    private final WalletRepository walletRepository;

    public RestaurantInformationKafkaListener(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    @KafkaListener(id = "${kafka-consumer-config.restaurant-information-consumer-group-id}",
                   topics = "${payment-service.restaurant-information-topic-name}")
    public void receive(@Payload List<RestaurantInformationAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of restaurant information messages received", messages.size());

        messages.forEach(avroModel -> {
            // Sadece "CREATED" veya ilk defa gelen eventlerde oluşturuyoruz.
            // Eğer update vs ise zaten veritabanında vardır.
            UUID restaurantId = avroModel.getRestaurantId();
            if (walletRepository.findByOwnerId(restaurantId).isEmpty()) {
                RestaurantWallet restaurantWallet = new RestaurantWallet(
                        new WalletId(UUID.randomUUID()),
                        restaurantId,
                        new Money(BigDecimal.ZERO)
                );
                walletRepository.save(restaurantWallet);
                log.info("Wallet successfully created for Restaurant with id: {}", restaurantId);
            }
        });
    }
}
