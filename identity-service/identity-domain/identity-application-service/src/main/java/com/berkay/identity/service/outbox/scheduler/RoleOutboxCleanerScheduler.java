package com.berkay.identity.service.outbox.scheduler;

import com.berkay.identity.service.outbox.helper.RoleOutboxHelper;
import com.berkay.identity.service.outbox.model.role.RoleOutboxMessage;
import com.berkay.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleOutboxCleanerScheduler {

    private final RoleOutboxHelper roleOutboxHelper;

    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        List<RoleOutboxMessage> outboxMessages =
                roleOutboxHelper.getRoleOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);

        if (!outboxMessages.isEmpty()) {
            log.info("Received {} RoleOutboxMessage...", outboxMessages.size());

            roleOutboxHelper.deleteRoleOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
            log.info("{} RoleOutboxMessage deleted!", outboxMessages.size());
        }
    }
}