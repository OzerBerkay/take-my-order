package com.berkay.payment.service.domain.strategy;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.payment.service.domain.PaymentDomainService;
import com.berkay.payment.service.domain.PaymentDomainServiceImpl;
import com.berkay.payment.service.domain.entity.OrderEscrow;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.event.PaymentEvent;
import com.berkay.payment.service.domain.ports.output.repository.OrderEscrowRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import com.berkay.payment.service.domain.valueobject.OrderEscrowStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletPaymentStrategyImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private OrderEscrowRepository orderEscrowRepository;

    private PaymentDomainService paymentDomainService = new PaymentDomainServiceImpl();

    private WalletPaymentStrategyImpl walletPaymentStrategy;

    private Payment payment;
    private Wallet customerWallet;

    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final UUID RESTAURANT_ID = UUID.randomUUID();
    private final UUID ORDER_ID = UUID.randomUUID();

    @BeforeEach
    public void setUp() {
        walletPaymentStrategy = new WalletPaymentStrategyImpl(walletRepository, orderEscrowRepository, paymentDomainService);

        payment = Payment.builder()
                .customerId(new CustomerId(CUSTOMER_ID))
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .orderId(new OrderId(ORDER_ID))
                .price(new Money(new BigDecimal("50.00")))
                .build();

        customerWallet = new com.berkay.payment.service.domain.entity.CustomerWallet(
                new com.berkay.payment.service.domain.valueobject.WalletId(UUID.randomUUID()),
                CUSTOMER_ID,
                new Money(new BigDecimal("100.00")));
    }

    @Test
    public void testProcessPayment_Success() {
        when(walletRepository.findByOwnerId(CUSTOMER_ID)).thenReturn(Optional.of(customerWallet));

        PaymentEvent paymentEvent = walletPaymentStrategy.processPayment(payment, new ArrayList<>());

        assertNotNull(paymentEvent);
        assertTrue(paymentEvent.getFailureMessages().isEmpty());
        assertEquals(new BigDecimal("50.00"), customerWallet.getBalance().getAmount());
        assertNotNull(paymentEvent.getCustomerWalletTransaction());
        assertNull(paymentEvent.getRestaurantWalletTransaction());
        assertNotNull(paymentEvent.getOrderEscrow());
        assertEquals(OrderEscrowStatus.HOLDING, paymentEvent.getOrderEscrow().getStatus());
        assertEquals(new BigDecimal("50.00"), paymentEvent.getOrderEscrow().getAmount().getAmount());
        assertEquals(ORDER_ID, paymentEvent.getOrderEscrow().getOrderId().getValue());
    }

    @Test
    public void testRefundPayment_Success() {
        when(walletRepository.findByOwnerId(CUSTOMER_ID)).thenReturn(Optional.of(customerWallet));

        OrderEscrow escrow = OrderEscrow.builder()
                .orderId(payment.getOrderId())
                .restaurantId(payment.getRestaurantId())
                .customerId(payment.getCustomerId())
                .amount(payment.getPrice())
                .build();
        escrow.initializeEscrow();

        when(orderEscrowRepository.findByOrderId(payment.getOrderId())).thenReturn(Optional.of(escrow));

        payment.initializePayment();
        payment.updateStatus(com.berkay.domain.valueobject.PaymentStatus.COMPLETED);
        customerWallet.subtractBalance(new Money(new BigDecimal("50.00")));

        PaymentEvent paymentEvent = walletPaymentStrategy.refundPayment(payment, new ArrayList<>());

        assertNotNull(paymentEvent);
        assertTrue(paymentEvent.getFailureMessages().isEmpty());
        assertEquals(new BigDecimal("100.00"), customerWallet.getBalance().getAmount());
        assertNotNull(paymentEvent.getCustomerWalletTransaction());
        assertNull(paymentEvent.getRestaurantWalletTransaction());
        assertNotNull(paymentEvent.getOrderEscrow());
        assertEquals(OrderEscrowStatus.REFUNDED, paymentEvent.getOrderEscrow().getStatus());
    }

    @Test
    public void testRefundPayment_LegacyFallback_Success() {
        when(walletRepository.findByOwnerId(CUSTOMER_ID)).thenReturn(Optional.of(customerWallet));
        when(orderEscrowRepository.findByOrderId(payment.getOrderId())).thenReturn(Optional.empty());

        com.berkay.payment.service.domain.entity.RestaurantWallet restaurantWallet =
                new com.berkay.payment.service.domain.entity.RestaurantWallet(
                        new com.berkay.payment.service.domain.valueobject.WalletId(UUID.randomUUID()),
                        RESTAURANT_ID,
                        new Money(new BigDecimal("200.00")));
        when(walletRepository.findByOwnerId(RESTAURANT_ID)).thenReturn(Optional.of(restaurantWallet));

        payment.initializePayment();
        payment.updateStatus(com.berkay.domain.valueobject.PaymentStatus.COMPLETED);
        customerWallet.subtractBalance(new Money(new BigDecimal("50.00")));

        PaymentEvent paymentEvent = walletPaymentStrategy.refundPayment(payment, new ArrayList<>());

        assertNotNull(paymentEvent);
        assertTrue(paymentEvent.getFailureMessages().isEmpty());
        assertEquals(new BigDecimal("100.00"), customerWallet.getBalance().getAmount());
        assertEquals(new BigDecimal("150.00"), restaurantWallet.getBalance().getAmount());
        assertNotNull(paymentEvent.getCustomerWalletTransaction());
        assertNotNull(paymentEvent.getRestaurantWalletTransaction());
        assertNull(paymentEvent.getOrderEscrow());
    }
}
