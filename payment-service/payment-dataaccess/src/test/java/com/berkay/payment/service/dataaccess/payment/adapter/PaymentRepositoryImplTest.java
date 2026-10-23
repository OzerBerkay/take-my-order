package com.berkay.payment.service.dataaccess.payment.adapter;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.PaymentStatus;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.payment.service.dataaccess.payment.entity.PaymentEntity;
import com.berkay.payment.service.dataaccess.payment.mapper.PaymentDataAccessMapper;
import com.berkay.payment.service.dataaccess.payment.repository.PaymentJpaRepository;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.valueobject.PaymentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentRepositoryImplTest {

    @Mock
    private PaymentJpaRepository paymentJpaRepository;

    private PaymentDataAccessMapper paymentDataAccessMapper;
    private PaymentRepositoryImpl paymentRepository;

    private final UUID paymentId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        paymentDataAccessMapper = new PaymentDataAccessMapper();
        paymentRepository = new PaymentRepositoryImpl(paymentJpaRepository, paymentDataAccessMapper);
    }

    @Test
    void save_whenExistingEntityFound_updatesManagedEntityInPlace() {
        // Arrange
        Payment domainPayment = Payment.builder()
                .paymentId(new PaymentId(paymentId))
                .customerId(new CustomerId(customerId))
                .restaurantId(new RestaurantId(restaurantId))
                .orderId(new OrderId(orderId))
                .price(new Money(new BigDecimal("150.00")))
                .paymentStatus(PaymentStatus.CANCELLED)
                .createdAt(ZonedDateTime.now())
                .version(0)
                .build();

        PaymentEntity existingEntity = PaymentEntity.builder()
                .id(paymentId)
                .customerId(customerId)
                .restaurantId(restaurantId)
                .orderId(orderId)
                .price(new BigDecimal("150.00"))
                .status(PaymentStatus.COMPLETED)
                .createdAt(domainPayment.getCreatedAt())
                .version(0)
                .build();

        when(paymentJpaRepository.findById(paymentId)).thenReturn(Optional.of(existingEntity));
        when(paymentJpaRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment savedPayment = paymentRepository.save(domainPayment);

        // Assert
        ArgumentCaptor<PaymentEntity> captor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentJpaRepository).save(captor.capture());

        PaymentEntity savedEntity = captor.getValue();
        // Crucial: Must be the same instance as existingEntity to prevent NonUniqueObjectException in Hibernate
        assertEquals(existingEntity, savedEntity);
        assertEquals(PaymentStatus.CANCELLED, savedEntity.getStatus());
        assertEquals(0, savedEntity.getVersion());
        assertEquals(PaymentStatus.CANCELLED, savedPayment.getPaymentStatus());
    }

    @Test
    void save_whenNewEntity_createsAndSavesNewEntity() {
        // Arrange
        Payment domainPayment = Payment.builder()
                .paymentId(new PaymentId(paymentId))
                .customerId(new CustomerId(customerId))
                .restaurantId(new RestaurantId(restaurantId))
                .orderId(new OrderId(orderId))
                .price(new Money(new BigDecimal("150.00")))
                .paymentStatus(PaymentStatus.COMPLETED)
                .createdAt(ZonedDateTime.now())
                .build();

        when(paymentJpaRepository.findById(paymentId)).thenReturn(Optional.empty());
        when(paymentJpaRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment savedPayment = paymentRepository.save(domainPayment);

        // Assert
        verify(paymentJpaRepository).save(any(PaymentEntity.class));
        assertEquals(PaymentStatus.COMPLETED, savedPayment.getPaymentStatus());
        assertEquals(paymentId, savedPayment.getId().getValue());
    }
}
