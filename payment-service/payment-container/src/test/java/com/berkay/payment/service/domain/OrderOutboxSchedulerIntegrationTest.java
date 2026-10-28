package com.berkay.payment.service.domain;

import com.berkay.outbox.OutboxStatus;
import com.berkay.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.berkay.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.berkay.payment.service.domain.outbox.scheduler.OrderOutboxScheduler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = PaymentServiceApplication.class)
public class OrderOutboxSchedulerIntegrationTest {

    @Autowired
    private OrderOutboxHelper orderOutboxHelper;

    @Autowired
    private OrderOutboxScheduler orderOutboxScheduler;

    @Test
    void testProcessStartedMessages() {
        assertNotNull(orderOutboxHelper);
        assertNotNull(orderOutboxScheduler);
        assertDoesNotThrow(() -> orderOutboxScheduler.processOutboxMessage());
    }
}
