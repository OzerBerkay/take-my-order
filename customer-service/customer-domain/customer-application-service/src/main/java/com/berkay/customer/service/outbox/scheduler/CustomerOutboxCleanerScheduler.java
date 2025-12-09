package com.berkay.customer.service.outbox.scheduler;

import com.berkay.customer.service.outbox.model.CustomerOutboxMessage;
import com.berkay.outbox.OutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class CustomerOutboxCleanerScheduler implements OutboxScheduler {

    private final CustomerOutboxHelper customerOutboxHelper;

    public CustomerOutboxCleanerScheduler(CustomerOutboxHelper customerOutboxHelper) {
        this.customerOutboxHelper = customerOutboxHelper;
    }

    @Override
    @Transactional
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<CustomerOutboxMessage>> outboxMessagesResponse =
                customerOutboxHelper.getCustomerOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);

        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<CustomerOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} CustomerOutboxMessage for clean-up!", outboxMessages.size());
            customerOutboxHelper.deleteCustomerOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
            log.info("Deleted {} CustomerOutboxMessage!", outboxMessages.size());
        }
    }
}
