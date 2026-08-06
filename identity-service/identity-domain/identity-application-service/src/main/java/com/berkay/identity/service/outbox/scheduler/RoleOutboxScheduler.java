package com.berkay.identity.service.outbox.scheduler;

import com.berkay.identity.service.outbox.helper.RoleOutboxHelper;
import com.berkay.identity.service.outbox.model.role.RoleOutboxMessage;
import com.berkay.identity.service.ports.output.message.publisher.RoleMessagePublisher;
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
public class RoleOutboxScheduler {

    private final RoleOutboxHelper roleOutboxHelper;
    private final RoleMessagePublisher roleMessagePublisher;

    @Transactional
    @Scheduled(fixedDelayString = "${identity-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${identity-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        List<RoleOutboxMessage> outboxMessages =
                roleOutboxHelper.getRoleOutboxMessageByOutboxStatus(OutboxStatus.STARTED);

        if (!outboxMessages.isEmpty()) {
            log.info("Received {} RoleOutboxMessage...", outboxMessages.size());

            outboxMessages.forEach(outboxMessage ->
                    roleMessagePublisher.publish(outboxMessage, this::updateOutboxStatus));
            log.info("{} RoleOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }

    private void updateOutboxStatus(RoleOutboxMessage roleOutboxMessage, OutboxStatus outboxStatus) {
        roleOutboxHelper.updateOutboxMessage(roleOutboxMessage, outboxStatus);
    }
}