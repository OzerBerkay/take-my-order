package com.berkay.identity.service.outbox.scheduler;

import com.berkay.identity.service.outbox.model.UserOutboxMessage;
import com.berkay.identity.service.ports.output.message.publisher.UserMessagePublisher;
import com.berkay.outbox.OutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserOutboxScheduler implements OutboxScheduler {

    private final UserOutboxHelper userOutboxHelper;
    private final UserMessagePublisher userMessagePublisher;

    public UserOutboxScheduler(UserOutboxHelper userOutboxHelper,
                               UserMessagePublisher userMessagePublisher) {
        this.userOutboxHelper = userOutboxHelper;
        this.userMessagePublisher = userMessagePublisher;
    }

    // Burada transactional anotasyonu bulunmaz çünkü  Veritabanından veriyi çektin, loop'a girdin, Kafka'ya gidiyorsun
    // (Network IO). Kafka yavaşsa veya timeout yerse, veritabanı transaction'ı o kadar süre açık kalır.
    // DB connection pool şişebilir.
    @Override
    @Scheduled(fixedDelayString = "${identity-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${identity-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        List<UserOutboxMessage> outboxMessages =
                userOutboxHelper.getUserOutboxMessageByOutboxStatus(OutboxStatus.STARTED);

        if (!outboxMessages.isEmpty()) {
            log.info("Received {} UserOutboxMessage with ids {}, sending to message bus!", outboxMessages.size(),
                    outboxMessages.stream().map(msg -> msg.getId().toString()).collect(Collectors.joining(",")));

            // Lambda içinde helper'ın update metodunu callback olarak veriyoruz
            outboxMessages.forEach(outboxMessage ->
                    userMessagePublisher.publish(outboxMessage, userOutboxHelper::updateOutboxMessage));

            log.info("{} UserOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }
}