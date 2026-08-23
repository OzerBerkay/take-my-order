package com.berkay.payment.service.domain.strategy;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.payment.service.domain.PaymentDomainService;
import com.berkay.payment.service.domain.PaymentDomainServiceImpl;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.event.PaymentEvent;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletPaymentStrategyImplTest {

    @Mock
    private WalletRepository walletRepository;

    private PaymentDomainService paymentDomainService = new PaymentDomainServiceImpl();

    private WalletPaymentStrategyImpl walletPaymentStrategy;

    private Payment payment;
    private Wallet customerWallet;
    private Wallet restaurantWallet;
    
    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final UUID RESTAURANT_ID = UUID.randomUUID();
    private final UUID ORDER_ID = UUID.randomUUID();

    @BeforeEach
    public void setUp() {
        walletPaymentStrategy = new WalletPaymentStrategyImpl(walletRepository, paymentDomainService);

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

        restaurantWallet = new com.berkay.payment.service.domain.entity.RestaurantWallet(
                new com.berkay.payment.service.domain.valueobject.WalletId(UUID.randomUUID()),
                RESTAURANT_ID,
                new Money(new BigDecimal("20.00")));
    }

    @Test
    public void testProcessPayment_Success() {
        when(walletRepository.findByOwnerId(CUSTOMER_ID)).thenReturn(Optional.of(customerWallet));
        when(walletRepository.findByOwnerId(RESTAURANT_ID)).thenReturn(Optional.of(restaurantWallet));

        PaymentEvent paymentEvent = walletPaymentStrategy.processPayment(payment, new ArrayList<>());

        assertNotNull(paymentEvent);
        assertTrue(paymentEvent.getFailureMessages().isEmpty());
        assertEquals(new BigDecimal("50.00"), customerWallet.getBalance().getAmount());
        assertEquals(new BigDecimal("70.00"), restaurantWallet.getBalance().getAmount());
        assertNotNull(paymentEvent.getCustomerWalletTransaction());
        assertNotNull(paymentEvent.getRestaurantWalletTransaction());
    }

    @Test
    public void testRefundPayment_Success() {
        when(walletRepository.findByOwnerId(CUSTOMER_ID)).thenReturn(Optional.of(customerWallet));
        when(walletRepository.findByOwnerId(RESTAURANT_ID)).thenReturn(Optional.of(restaurantWallet));
        
        // Önce ödeme yapıp tamamlandığını varsayıyoruz ki iptal edilebilsin
        payment.initializePayment();
        payment.updateStatus(com.berkay.domain.valueobject.PaymentStatus.COMPLETED);
        restaurantWallet.addBalance(new Money(new BigDecimal("50.00"))); // Restoranın parayı aldığını varsayıyoruz
        customerWallet.subtractBalance(new Money(new BigDecimal("50.00"))); // Müşterinin parayı verdiğini varsayıyoruz

        PaymentEvent paymentEvent = walletPaymentStrategy.refundPayment(payment, new ArrayList<>());

        assertNotNull(paymentEvent);
        assertTrue(paymentEvent.getFailureMessages().isEmpty());
        // Customer geri alır, restoran kaybeder.
        assertEquals(new BigDecimal("100.00"), customerWallet.getBalance().getAmount());
        assertEquals(new BigDecimal("20.00"), restaurantWallet.getBalance().getAmount());
        assertNotNull(paymentEvent.getCustomerWalletTransaction());
        assertNotNull(paymentEvent.getRestaurantWalletTransaction());
    }
}
