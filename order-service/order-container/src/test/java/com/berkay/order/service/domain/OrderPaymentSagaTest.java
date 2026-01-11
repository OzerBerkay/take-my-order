package com.berkay.order.service.domain;

import com.berkay.order.service.application.OrderServiceApplication;
import com.berkay.order.service.dataaccess.outbox.payment.entity.PaymentOutboxEntity;
import com.berkay.order.service.dataaccess.outbox.payment.repository.PaymentOutboxJpaRepository;
import com.berkay.order.service.domain.dto.message.PaymentResponse;
import com.berkay.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static com.berkay.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Slf4j
@SpringBootTest(classes = OrderServiceApplication.class,
        properties = {
                "ORDER_SERVICE_PORT=8181",
                "order-service.restaurant-created-topic-name=restaurant-created",
                "order-service.payment-response-topic-name=payment-response",
                "order-service.payment-request-topic-name=payment-request",
                "order-service.restaurant-approval-request-topic-name=restaurant-approval-request",
                "order-service.restaurant-approval-response-topic-name=restaurant-approval-response",
                "kafka-consumer-config.restaurant-created-consumer-group-id=restaurant-created-topic-consumer",
                "kafka-consumer-config.payment-consumer-group-id=payment-topic-consumer",
                "kafka-consumer-config.restaurant-approval-consumer-group-id=restaurant-approval-topic-consumer"
        }
)
// CleanUp hem başta (önceki kalıntıları temizle) hem sonda (kendi kalıntılarını temizle) çalışacak.
@Sql(value = {"classpath:sql/OrderPaymentSagaTestCleanUp.sql", "classpath:sql/OrderPaymentSagaTestSetUp.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(value = {"classpath:sql/OrderPaymentSagaTestCleanUp.sql"}, executionPhase = AFTER_TEST_METHOD)
public class OrderPaymentSagaTest {

    @Autowired
    private OrderPaymentSaga orderPaymentSaga;

    @Autowired
    private PaymentOutboxJpaRepository paymentOutboxJpaRepository;

    // SQL dosyasındaki verilerle UYUMLU sabit ID'ler
    private final UUID SAGA_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8c07afa");
    private final UUID ORDER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb17");
    private final UUID CUSTOMER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41");
    private final UUID PAYMENT_ID = UUID.randomUUID(); // Bu dinamik olabilir, sorun yok
    private final BigDecimal PRICE = new BigDecimal("100");

    @Test
    void testDoublePayment() {
        // İlk çağrı
        orderPaymentSaga.process(getPaymentResponse());

        // İkinci çağrı (Duplicate Key hatası bekleniyor)
        try {
            orderPaymentSaga.process(getPaymentResponse());
        } catch (DataIntegrityViolationException e) {
            log.info("DataIntegrityViolationException caught as expected for testDoublePayment");
        }
    }

    @Test
    void testDoublePaymentWithThreads() throws InterruptedException {
        Thread thread1 = new Thread(() -> processPayment());
        Thread thread2 = new Thread(() -> processPayment());

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertPaymentOutbox();
    }

    @Test
    void testDoublePaymentWithLatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        Thread thread1 = new Thread(() -> {
            try {
                processPayment();
            } finally {
                latch.countDown();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                processPayment();
            } finally {
                latch.countDown();
            }
        });

        thread1.start();
        thread2.start();

        latch.await();

        assertPaymentOutbox();
    }

    // Helper Metod
    private void processPayment() {
        try {
            orderPaymentSaga.process(getPaymentResponse());
        } catch (DataIntegrityViolationException e) {
            log.info("DataIntegrityViolationException caught as expected in thread execution");
        } catch (Exception e) {
            log.error("Unexpected exception: {}", e.getMessage());
        }
    }

    private void assertPaymentOutbox() {
        Optional<PaymentOutboxEntity> paymentOutboxEntity =
                paymentOutboxJpaRepository.findByTypeAndSagaIdAndSagaStatusIn(ORDER_SAGA_NAME, SAGA_ID,
                        List.of(SagaStatus.PROCESSING));
        assertTrue(paymentOutboxEntity.isPresent(), "Payment Outbox should be present!");
    }

    private PaymentResponse getPaymentResponse() {
        return PaymentResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(SAGA_ID.toString())
                .paymentStatus(com.berkay.domain.valueobject.PaymentStatus.COMPLETED)
                .paymentId(PAYMENT_ID.toString())
                .orderId(ORDER_ID.toString())
                .customerId(CUSTOMER_ID.toString())
                .price(PRICE)
                .createdAt(Instant.now())
                .failureMessages(new ArrayList<>())
                .build();
    }
}