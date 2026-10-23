package com.berkay.payment.service.dataaccess.payment.adapter;

import com.berkay.payment.service.dataaccess.payment.entity.PaymentEntity;
import com.berkay.payment.service.dataaccess.payment.mapper.PaymentDataAccessMapper;
import com.berkay.payment.service.dataaccess.payment.repository.PaymentJpaRepository;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.ports.output.repository.PaymentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentDataAccessMapper paymentDataAccessMapper;

    public PaymentRepositoryImpl(PaymentJpaRepository paymentJpaRepository,
                                 PaymentDataAccessMapper paymentDataAccessMapper) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.paymentDataAccessMapper = paymentDataAccessMapper;
    }

    @Override
    public Payment save(Payment payment) {
        Optional<PaymentEntity> existingPaymentEntity = paymentJpaRepository.findById(payment.getId().getValue());
        PaymentEntity paymentEntity;
        if (existingPaymentEntity.isPresent()) {
            paymentEntity = existingPaymentEntity.get();
            paymentEntity.setCustomerId(payment.getCustomerId().getValue());
            paymentEntity.setRestaurantId(payment.getRestaurantId() != null ? payment.getRestaurantId().getValue() : null);
            paymentEntity.setOrderId(payment.getOrderId().getValue());
            paymentEntity.setPrice(payment.getPrice().getAmount());
            paymentEntity.setStatus(payment.getPaymentStatus());
            paymentEntity.setCreatedAt(payment.getCreatedAt());
        } else {
            paymentEntity = paymentDataAccessMapper.paymentToPaymentEntity(payment);
        }
        return paymentDataAccessMapper
                .paymentEntityToPayment(paymentJpaRepository.save(paymentEntity));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId)
                .map(paymentDataAccessMapper::paymentEntityToPayment);
    }
}
