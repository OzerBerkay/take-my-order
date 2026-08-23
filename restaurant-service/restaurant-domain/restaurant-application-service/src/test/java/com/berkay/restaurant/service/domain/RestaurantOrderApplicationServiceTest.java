package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderApprovalStatus;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.dto.approve.ApproveRestaurantOrderCommand;

import com.berkay.restaurant.service.domain.dto.reject.RejectRestaurantOrderCommand;
import com.berkay.restaurant.service.domain.entity.OrderApproval;
import com.berkay.restaurant.service.domain.entity.OrderDetail;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.berkay.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import com.berkay.restaurant.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.valueobject.OrderApprovalId;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantOrderApplicationServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private OrderApprovalRepository orderApprovalRepository;

    @Mock
    private OrderOutboxHelper orderOutboxHelper;

    @Mock
    private RestaurantDataMapper restaurantDataMapper;

    @InjectMocks
    private ApproveRestaurantOrderCommandHandler approveRestaurantOrderCommandHandler;

    @InjectMocks
    private RejectRestaurantOrderCommandHandler rejectRestaurantOrderCommandHandler;


    private UUID restaurantIdValue;
    private UUID orderIdValue;
    private Restaurant restaurant;
    private OrderApproval orderApproval;

    @BeforeEach
    void setUp() {
        restaurantIdValue = UUID.randomUUID();
        orderIdValue = UUID.randomUUID();

        orderApproval = OrderApproval.builder()
                .orderApprovalId(new OrderApprovalId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(restaurantIdValue))
                .orderId(new OrderId(orderIdValue))
                .approvalStatus(OrderApprovalStatus.PENDING)
                .build();

        restaurant = Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantIdValue))
                .orderApproval(orderApproval)
                .orderDetail(OrderDetail.builder().orderId(new OrderId(orderIdValue)).build())
                .build();
    }

    @Test
    void approveOrder_ShouldSaveOutboxMessage_WhenOrderIsPending() {
        ApproveRestaurantOrderCommand command = ApproveRestaurantOrderCommand.builder()
                .restaurantId(restaurantIdValue)
                .orderId(orderIdValue)
                .build();

        when(restaurantRepository.findRestaurantById(restaurantIdValue)).thenReturn(Optional.of(restaurant));
        when(orderApprovalRepository.findByRestaurantIdAndOrderId(restaurantIdValue, orderIdValue)).thenReturn(Optional.of(orderApproval));
        when(restaurantDataMapper.orderApprovalEventToOrderEventPayload(any())).thenReturn(com.berkay.restaurant.service.domain.outbox.model.OrderEventPayload.builder().build());

        approveRestaurantOrderCommandHandler.approveOrder(command);

        verify(orderOutboxHelper, times(1)).saveOrderOutboxMessage(
                any(),
                eq(OrderApprovalStatus.APPROVED),
                eq(OutboxStatus.STARTED),
                eq(orderIdValue)
        );
        
        verify(orderApprovalRepository, times(1)).save(any(OrderApproval.class));
    }

    @Test
    void rejectOrder_ShouldSaveOutboxMessageWithFailures_WhenOrderIsPending() {
        RejectRestaurantOrderCommand command = RejectRestaurantOrderCommand.builder()
                .restaurantId(restaurantIdValue)
                .orderId(orderIdValue)
                .failureMessages(List.of("Out of tomatoes"))
                .build();

        when(restaurantRepository.findRestaurantById(restaurantIdValue)).thenReturn(Optional.of(restaurant));
        when(orderApprovalRepository.findByRestaurantIdAndOrderId(restaurantIdValue, orderIdValue)).thenReturn(Optional.of(orderApproval));
        when(restaurantDataMapper.orderApprovalEventToOrderEventPayload(any())).thenReturn(com.berkay.restaurant.service.domain.outbox.model.OrderEventPayload.builder().build());


        rejectRestaurantOrderCommandHandler.rejectOrder(command);

        verify(orderOutboxHelper, times(1)).saveOrderOutboxMessage(
                any(),
                eq(OrderApprovalStatus.REJECTED),
                eq(OutboxStatus.STARTED),
                eq(orderIdValue)
        );
        
        verify(orderApprovalRepository, times(1)).save(any(OrderApproval.class));
    }

}
