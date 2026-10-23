package com.berkay.payment.service.dataaccess.payment.mapper;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.payment.service.dataaccess.payment.entity.PaymentEntity;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.valueobject.PaymentId;
import org.springframework.stereotype.Component;

@Component
public class PaymentDataAccessMapper {

    public PaymentEntity paymentToPaymentEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId().getValue())
                .customerId(payment.getCustomerId().getValue())
                .restaurantId(payment.getRestaurantId() != null ? payment.getRestaurantId().getValue() : null)
                .orderId(payment.getOrderId().getValue())
                .price(payment.getPrice().getAmount())
                .status(payment.getPaymentStatus())
                .createdAt(payment.getCreatedAt())
                .version(payment.getVersion())
                .build();
    }

    public Payment paymentEntityToPayment(PaymentEntity paymentEntity) {
        return Payment.builder()
                .paymentId(new PaymentId(paymentEntity.getId()))
                .customerId(new CustomerId(paymentEntity.getCustomerId()))
                .restaurantId(paymentEntity.getRestaurantId() != null ? new RestaurantId(paymentEntity.getRestaurantId()) : null)
                .orderId(new OrderId(paymentEntity.getOrderId()))
                .price(new Money(paymentEntity.getPrice()))
                .paymentStatus(paymentEntity.getStatus())
                .createdAt(paymentEntity.getCreatedAt())
                .version(paymentEntity.getVersion())
                .build();
    }

}
