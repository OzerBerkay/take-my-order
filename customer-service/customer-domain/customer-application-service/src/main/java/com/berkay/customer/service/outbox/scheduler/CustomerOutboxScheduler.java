package com.berkay.customer.service.outbox.scheduler;

import com.berkay.customer.service.outbox.model.CustomerOutboxMessage;
import com.berkay.customer.service.ports.output.message.publisher.CustomerMessagePublisher;
import com.berkay.outbox.OutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomerOutboxScheduler implements OutboxScheduler {

    private final CustomerOutboxHelper customerOutboxHelper;
    private final CustomerMessagePublisher customerMessagePublisher;

    public CustomerOutboxScheduler(CustomerOutboxHelper customerOutboxHelper,
                                   CustomerMessagePublisher customerMessagePublisher) {
        this.customerOutboxHelper = customerOutboxHelper;
        this.customerMessagePublisher = customerMessagePublisher;
    }

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${customer-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${customer-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<CustomerOutboxMessage>> outboxMessagesResponse =
                customerOutboxHelper.getCustomerOutboxMessageByOutboxStatus(OutboxStatus.STARTED);

        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<CustomerOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} CustomerOutboxMessage with ids {}, sending to message bus!", outboxMessages.size(),
                    outboxMessages.stream().map(outboxMessage ->
                            outboxMessage.getId().toString()).collect(Collectors.joining(",")));

            // DİKKAT: Publisher'ın bu imzayı desteklemesi gerekecek!
            outboxMessages.forEach(outboxMessage ->
                    customerMessagePublisher.publish(outboxMessage,
                            customerOutboxHelper::updateOutboxMessage));

            log.info("{} CustomerOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }
}
