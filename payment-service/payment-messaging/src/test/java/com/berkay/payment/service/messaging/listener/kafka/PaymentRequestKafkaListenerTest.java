package com.berkay.payment.service.messaging.listener.kafka;

import com.berkay.kafka.order.avro.model.PaymentOrderStatus;
import com.berkay.kafka.order.avro.model.PaymentRequestAvroModel;
import com.berkay.payment.service.domain.dto.PaymentRequest;
import com.berkay.payment.service.domain.exception.PaymentApplicationServiceException;
import com.berkay.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.berkay.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentRequestKafkaListenerTest {

    @Mock
    private PaymentRequestMessageListener paymentRequestMessageListener;

    @Mock
    private PaymentMessagingDataMapper paymentMessagingDataMapper;

    private PaymentRequestKafkaListener listener;

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        listener = new PaymentRequestKafkaListener(paymentRequestMessageListener, paymentMessagingDataMapper);
    }

    private PaymentRequestAvroModel createAvroModel(PaymentOrderStatus status) {
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId(UUID.randomUUID().toString())
                .setCustomerId(customerId.toString())
                .setRestaurantId(restaurantId.toString())
                .setOrderId(orderId.toString())
                .setPrice(new BigDecimal("100.00"))
                .setCreatedAt(Instant.now())
                .setPaymentOrderStatus(status)
                .build();
    }

    @Test
    void receive_completePayment_success() {
        PaymentRequestAvroModel model = createAvroModel(PaymentOrderStatus.PENDING);
        when(paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(any())).thenReturn(
                PaymentRequest.builder().build());

        listener.receive(List.of(model), List.of("key"), List.of(0), List.of(0L));

        verify(paymentRequestMessageListener).completePayment(any());
        verify(paymentRequestMessageListener, never()).cancelPayment(any());
    }

    @Test
    void receive_cancelPayment_success() {
        PaymentRequestAvroModel model = createAvroModel(PaymentOrderStatus.CANCELLED);
        when(paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(any())).thenReturn(
                PaymentRequest.builder().build());

        listener.receive(List.of(model), List.of("key"), List.of(0), List.of(0L));

        verify(paymentRequestMessageListener).cancelPayment(any());
        verify(paymentRequestMessageListener, never()).completePayment(any());
    }

    @Test
    void receive_whenNonSqlExceptionRootCause_throwsPaymentApplicationServiceExceptionWithoutClassCastException() {
        PaymentRequestAvroModel model = createAvroModel(PaymentOrderStatus.CANCELLED);
        when(paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(any())).thenReturn(
                PaymentRequest.builder().build());

        // Simulate a non-SQLException root cause (e.g. Hibernate exception) wrapped in Spring DataAccessException
        RuntimeException nonSqlException = new RuntimeException("Hibernate conflict");
        DataAccessException springException = new DataAccessException("Data access error", nonSqlException) {};

        doThrow(springException).when(paymentRequestMessageListener).cancelPayment(any());

        // Crucial test: It must throw PaymentApplicationServiceException, NOT ClassCastException!
        assertThrows(PaymentApplicationServiceException.class, () ->
                listener.receive(List.of(model), List.of("key"), List.of(0), List.of(0L)));
    }

    @Test
    void receive_whenUniqueConstraintViolation_doesNotThrow() {
        PaymentRequestAvroModel model = createAvroModel(PaymentOrderStatus.CANCELLED);
        when(paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(any())).thenReturn(
                PaymentRequest.builder().build());

        SQLException sqlException = new SQLException("duplicate key", PSQLState.UNIQUE_VIOLATION.getState());
        DataAccessException springException = new DataAccessException("Unique violation", sqlException) {};

        doThrow(springException).when(paymentRequestMessageListener).cancelPayment(any());

        assertDoesNotThrow(() ->
                listener.receive(List.of(model), List.of("key"), List.of(0), List.of(0L)));
    }
}
