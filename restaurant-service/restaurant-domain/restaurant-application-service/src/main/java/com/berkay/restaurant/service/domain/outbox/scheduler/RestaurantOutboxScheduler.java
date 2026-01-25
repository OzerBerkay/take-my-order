package com.berkay.restaurant.service.domain.outbox.scheduler;

import com.berkay.outbox.OutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import com.berkay.restaurant.service.domain.ports.output.RestaurantInformationMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestaurantOutboxScheduler implements OutboxScheduler {

    private final RestaurantOutboxHelper restaurantOutboxHelper;
    private final RestaurantInformationMessagePublisher restaurantInformationMessagePublisher;

    public RestaurantOutboxScheduler(RestaurantOutboxHelper restaurantOutboxHelper,
                                     RestaurantInformationMessagePublisher restaurantInformationMessagePublisher) {
        this.restaurantOutboxHelper = restaurantOutboxHelper;
        this.restaurantInformationMessagePublisher = restaurantInformationMessagePublisher;
    }

    // @Transactional anotasyonu burada değil updateOutboxMessage metodu icinde kullanılır.
    // Eğer burada kullanılırsa Kafka'ya 10 mesajın 9u başarılı 1'i başarısız giderse, hepsi başarısızmış gibi rollback olur
    // bu da duplicate event yayınlamayla sonuçlanır
    @Override
    @Scheduled(fixedDelayString = "${restaurant-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${restaurant-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        List<RestaurantOutboxMessage> outboxMessages =
                restaurantOutboxHelper.getRestaurantOutboxMessageByOutboxStatus(OutboxStatus.STARTED);

        if (!outboxMessages.isEmpty()) {
            log.info("Received {} RestaurantOutboxMessage with id: {}, sending to message bus!",
                    outboxMessages.size(),
                    outboxMessages.stream().map(message -> message.getId().toString())
                            .collect(Collectors.joining(",")));

            outboxMessages.forEach(outboxMessage ->
                    restaurantInformationMessagePublisher.publish(outboxMessage, restaurantOutboxHelper::updateOutboxMessage));

            log.info("{} RestaurantOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }
}
