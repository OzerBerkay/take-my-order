package com.berkay.identity.service.outbox.scheduler;

import com.berkay.outbox.OutboxScheduler;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserOutboxCleanerScheduler implements OutboxScheduler {

    private final UserOutboxHelper userOutboxHelper;

    public UserOutboxCleanerScheduler(UserOutboxHelper userOutboxHelper) {
        this.userOutboxHelper = userOutboxHelper;
    }

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        // Listeyi çekmeden doğrudan siliyoruz çünkü bir filtrelemeye ihtiyacımız yok bu sebeple listeyi çekip hafızayı şişirmemize de gerek yok
        userOutboxHelper.deleteUserOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
        log.info("UserOutboxCleanerScheduler finished cleaning up COMPLETED messages.");
    }
}