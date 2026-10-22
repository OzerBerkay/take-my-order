package com.berkay.order.service.messaging.listener.kafka;

import com.berkay.kafka.order.avro.model.OrderApprovalStatus;
import com.berkay.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.berkay.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.berkay.order.service.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;
import com.berkay.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantApprovalResponseKafkaListenerTest {

    @Mock
    private RestaurantApprovalResponseMessageListener restaurantApprovalResponseMessageListener;

    @Mock
    private OrderMessagingDataMapper orderMessagingDataMapper;

    @InjectMocks
    private RestaurantApprovalResponseKafkaListener restaurantApprovalResponseKafkaListener;

    private RestaurantApprovalResponseAvroModel validatedAvroModel;
    private RestaurantApprovalResponseAvroModel approvedAvroModel;
    private RestaurantApprovalResponseAvroModel rejectedAvroModel;
    private RestaurantApprovalResponse mappedResponse;

    @BeforeEach
    void setUp() {
        String orderId = UUID.randomUUID().toString();
        String sagaId = UUID.randomUUID().toString();
        String restaurantId = UUID.randomUUID().toString();

        validatedAvroModel = RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId(sagaId)
                .setOrderId(orderId)
                .setRestaurantId(restaurantId)
                .setCreatedAt(Instant.now())
                .setOrderApprovalStatus(OrderApprovalStatus.VALIDATED)
                .setFailureMessages(List.of())
                .setCancelReason(null)
                .build();

        approvedAvroModel = RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId(sagaId)
                .setOrderId(orderId)
                .setRestaurantId(restaurantId)
                .setCreatedAt(Instant.now())
                .setOrderApprovalStatus(OrderApprovalStatus.APPROVED)
                .setFailureMessages(List.of())
                .setCancelReason(null)
                .build();

        rejectedAvroModel = RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId(sagaId)
                .setOrderId(orderId)
                .setRestaurantId(restaurantId)
                .setCreatedAt(Instant.now())
                .setOrderApprovalStatus(OrderApprovalStatus.REJECTED)
                .setFailureMessages(List.of("Out of stock"))
                .setCancelReason("AUTO_REJECTED_OUT_OF_STOCK")
                .build();

        mappedResponse = RestaurantApprovalResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId)
                .orderId(orderId)
                .restaurantId(restaurantId)
                .build();
    }

    @Test
    void shouldProcessValidatedOrderApprovalResponse() {
        when(orderMessagingDataMapper.approvalResponseAvroModelToApprovalResponse(validatedAvroModel))
                .thenReturn(mappedResponse);

        restaurantApprovalResponseKafkaListener.receive(
                List.of(validatedAvroModel),
                List.of("key"),
                List.of(0),
                List.of(0L)
        );

        verify(restaurantApprovalResponseMessageListener, times(1)).orderApproved(mappedResponse);
        verify(restaurantApprovalResponseMessageListener, never()).orderRejected(any());
    }

    @Test
    void shouldProcessApprovedOrderApprovalResponse() {
        when(orderMessagingDataMapper.approvalResponseAvroModelToApprovalResponse(approvedAvroModel))
                .thenReturn(mappedResponse);

        restaurantApprovalResponseKafkaListener.receive(
                List.of(approvedAvroModel),
                List.of("key"),
                List.of(0),
                List.of(0L)
        );

        verify(restaurantApprovalResponseMessageListener, times(1)).orderApproved(mappedResponse);
        verify(restaurantApprovalResponseMessageListener, never()).orderRejected(any());
    }

    @Test
    void shouldProcessRejectedOrderApprovalResponse() {
        when(orderMessagingDataMapper.approvalResponseAvroModelToApprovalResponse(rejectedAvroModel))
                .thenReturn(mappedResponse);

        restaurantApprovalResponseKafkaListener.receive(
                List.of(rejectedAvroModel),
                List.of("key"),
                List.of(0),
                List.of(0L)
        );

        verify(restaurantApprovalResponseMessageListener, times(1)).orderRejected(mappedResponse);
        verify(restaurantApprovalResponseMessageListener, never()).orderApproved(any());
    }
}
