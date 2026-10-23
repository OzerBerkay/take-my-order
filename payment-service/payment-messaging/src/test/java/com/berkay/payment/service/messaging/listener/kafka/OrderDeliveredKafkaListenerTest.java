package com.berkay.payment.service.messaging.listener.kafka;

import com.berkay.kafka.order.avro.model.OrderDeliveredAvroModel;
import com.berkay.payment.service.domain.dto.OrderDeliveredRequest;
import com.berkay.payment.service.domain.ports.input.message.listener.OrderDeliveredMessageListener;
import com.berkay.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderDeliveredKafkaListenerTest {

    @Mock
    private OrderDeliveredMessageListener orderDeliveredMessageListener;

    private PaymentMessagingDataMapper paymentMessagingDataMapper;
    private OrderDeliveredKafkaListener listener;

    @BeforeEach
    void setUp() {
        paymentMessagingDataMapper = new PaymentMessagingDataMapper();
        listener = new OrderDeliveredKafkaListener(orderDeliveredMessageListener, paymentMessagingDataMapper);
    }

    @Test
    void testReceive_CallsSettleOrder() {
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderDeliveredAvroModel avroModel = OrderDeliveredAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(orderId.toString())
                .setRestaurantId(restaurantId.toString())
                .setCustomerId(customerId.toString())
                .setPrice(new BigDecimal("99.50"))
                .setCreatedAt(Instant.now())
                .build();

        listener.receive(List.of(avroModel), List.of("key"), List.of(0), List.of(1L));

        ArgumentCaptor<OrderDeliveredRequest> captor = ArgumentCaptor.forClass(OrderDeliveredRequest.class);
        verify(orderDeliveredMessageListener).settleOrder(captor.capture());

        OrderDeliveredRequest captured = captor.getValue();
        assertEquals(orderId.toString(), captured.getOrderId());
        assertEquals(restaurantId.toString(), captured.getRestaurantId());
        assertEquals(new BigDecimal("99.50"), captured.getPrice());
    }
}
