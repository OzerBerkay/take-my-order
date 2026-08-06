package com.berkay.identity.service.outbox.scheduler;

import com.berkay.identity.service.outbox.helper.PermissionOutboxHelper;
import com.berkay.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionOutboxCleanerScheduler {

    private final PermissionOutboxHelper permissionOutboxHelper;

    @Transactional
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        log.info("Started cleaning up completed permission outbox messages...");
        permissionOutboxHelper.deletePermissionOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
        log.info("Finished cleaning up completed permission outbox messages...");
    }
}
