package com.berkay.identity.service.outbox.scheduler;

import com.berkay.identity.service.outbox.helper.PermissionOutboxHelper;
import com.berkay.identity.service.outbox.model.permission.PermissionOutboxMessage;
import com.berkay.identity.service.ports.output.message.publisher.PermissionMessagePublisher;
import com.berkay.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionOutboxScheduler {

    private final PermissionOutboxHelper permissionOutboxHelper;
    private final PermissionMessagePublisher permissionMessagePublisher;

    @Transactional
    @Scheduled(fixedDelayString = "${identity-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${identity-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<PermissionOutboxMessage>> outboxMessagesResponse =
                permissionOutboxHelper.getPermissionOutboxMessageByOutboxStatus(OutboxStatus.STARTED);

        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<PermissionOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} PermissionOutboxMessage...", outboxMessages.size());

            outboxMessages.forEach(outboxMessage ->
                    permissionMessagePublisher.publish(outboxMessage, this::updateOutboxStatus));
            log.info("{} PermissionOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }

    private void updateOutboxStatus(PermissionOutboxMessage permissionOutboxMessage, OutboxStatus outboxStatus) {
        permissionOutboxHelper.updateOutboxMessage(permissionOutboxMessage, outboxStatus);
    }
}
