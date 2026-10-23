package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.payment.service.domain.dto.OrderDeliveredRequest;
import com.berkay.payment.service.domain.entity.OrderEscrow;
import com.berkay.payment.service.domain.entity.RestaurantWallet;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.ports.output.repository.OrderEscrowRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletTransactionRepository;
import com.berkay.payment.service.domain.valueobject.OrderEscrowStatus;
import com.berkay.payment.service.domain.valueobject.WalletId;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderDeliveredMessageListenerImplTest {

    @Mock
    private OrderEscrowRepository orderEscrowRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    private OrderDeliveredMessageListenerImpl listener;

    private final UUID ORDER_ID = UUID.randomUUID();
    private final UUID RESTAURANT_ID = UUID.randomUUID();
    private final UUID CUSTOMER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        listener = new OrderDeliveredMessageListenerImpl(orderEscrowRepository, walletRepository, walletTransactionRepository);
    }

    @Test
    void testSettleOrderSuccess() {
        OrderEscrow escrow = OrderEscrow.builder()
                .orderId(new OrderId(ORDER_ID))
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .customerId(new CustomerId(CUSTOMER_ID))
                .amount(new Money(new BigDecimal("150.00")))
                .status(OrderEscrowStatus.HOLDING)
                .build();

        Wallet restaurantWallet = new RestaurantWallet(
                new WalletId(UUID.randomUUID()),
                RESTAURANT_ID,
                new Money(new BigDecimal("50.00"))
        );

        when(orderEscrowRepository.findByOrderIdWithLock(new OrderId(ORDER_ID))).thenReturn(Optional.of(escrow));
        when(walletRepository.findByOwnerIdWithLock(RESTAURANT_ID)).thenReturn(Optional.of(restaurantWallet));

        OrderDeliveredRequest request = OrderDeliveredRequest.builder()
                .orderId(ORDER_ID.toString())
                .restaurantId(RESTAURANT_ID.toString())
                .customerId(CUSTOMER_ID.toString())
                .price(new BigDecimal("150.00"))
                .createdAt(Instant.now())
                .build();

        listener.settleOrder(request);

        assertEquals(OrderEscrowStatus.RELEASED, escrow.getStatus());
        assertEquals(new BigDecimal("200.00"), restaurantWallet.getBalance().getAmount());
        verify(orderEscrowRepository).save(escrow);
        verify(walletRepository).save(restaurantWallet);
        ArgumentCaptor<WalletTransaction> transactionCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(transactionCaptor.capture());
        assertEquals(WalletTransactionType.ORDER_PAYOUT, transactionCaptor.getValue().getTransactionType());
        assertEquals(ORDER_ID.toString(), transactionCaptor.getValue().getReferenceId());
        assertEquals(new BigDecimal("150.00"), transactionCaptor.getValue().getAmount().getAmount());
    }

    @Test
    void testSettleOrderIdempotentWhenAlreadyReleased() {
        OrderEscrow escrow = OrderEscrow.builder()
                .orderId(new OrderId(ORDER_ID))
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .customerId(new CustomerId(CUSTOMER_ID))
                .amount(new Money(new BigDecimal("150.00")))
                .status(OrderEscrowStatus.RELEASED)
                .build();

        when(orderEscrowRepository.findByOrderIdWithLock(new OrderId(ORDER_ID))).thenReturn(Optional.of(escrow));

        OrderDeliveredRequest request = OrderDeliveredRequest.builder()
                .orderId(ORDER_ID.toString())
                .restaurantId(RESTAURANT_ID.toString())
                .customerId(CUSTOMER_ID.toString())
                .price(new BigDecimal("150.00"))
                .createdAt(Instant.now())
                .build();

        listener.settleOrder(request);

        verify(walletRepository, never()).findByOwnerIdWithLock(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void testSettleOrderSkipWhenRefunded() {
        OrderEscrow escrow = OrderEscrow.builder()
                .orderId(new OrderId(ORDER_ID))
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .customerId(new CustomerId(CUSTOMER_ID))
                .amount(new Money(new BigDecimal("150.00")))
                .status(OrderEscrowStatus.REFUNDED)
                .build();

        when(orderEscrowRepository.findByOrderIdWithLock(new OrderId(ORDER_ID))).thenReturn(Optional.of(escrow));

        OrderDeliveredRequest request = OrderDeliveredRequest.builder()
                .orderId(ORDER_ID.toString())
                .restaurantId(RESTAURANT_ID.toString())
                .customerId(CUSTOMER_ID.toString())
                .price(new BigDecimal("150.00"))
                .createdAt(Instant.now())
                .build();

        listener.settleOrder(request);

        verify(walletRepository, never()).save(any());
    }

    @Test
    void testSettleOrderNotFoundGraceful() {
        when(orderEscrowRepository.findByOrderIdWithLock(new OrderId(ORDER_ID))).thenReturn(Optional.empty());

        OrderDeliveredRequest request = OrderDeliveredRequest.builder()
                .orderId(ORDER_ID.toString())
                .restaurantId(RESTAURANT_ID.toString())
                .customerId(CUSTOMER_ID.toString())
                .price(new BigDecimal("150.00"))
                .createdAt(Instant.now())
                .build();

        listener.settleOrder(request);

        verify(walletRepository, never()).save(any());
    }
}
