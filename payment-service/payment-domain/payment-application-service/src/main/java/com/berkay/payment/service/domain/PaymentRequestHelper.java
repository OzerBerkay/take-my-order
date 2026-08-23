package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.PaymentStatus;
import com.berkay.outbox.OutboxStatus;
import com.berkay.payment.service.domain.dto.PaymentRequest;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.event.PaymentEvent;
import com.berkay.payment.service.domain.exception.PaymentApplicationServiceException;
import com.berkay.payment.service.domain.exception.PaymentNotFoundException;
import com.berkay.payment.service.domain.mapper.PaymentDataMapper;
import com.berkay.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.berkay.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.berkay.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import com.berkay.payment.service.domain.ports.output.repository.PaymentRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletTransactionRepository;
import com.berkay.payment.service.domain.strategy.PaymentProcessorStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class PaymentRequestHelper {

    private final List<PaymentProcessorStrategy> paymentStrategies;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final OrderOutboxHelper orderOutboxHelper;
    private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;

    public PaymentRequestHelper(List<PaymentProcessorStrategy> paymentStrategies,
                                PaymentDataMapper paymentDataMapper,
                                PaymentRepository paymentRepository,
                                WalletRepository walletRepository,
                                WalletTransactionRepository walletTransactionRepository,
                                OrderOutboxHelper orderOutboxHelper,
                                PaymentResponseMessagePublisher paymentResponseMessagePublisher) {
        this.paymentStrategies = paymentStrategies;
        this.paymentDataMapper = paymentDataMapper;
        this.paymentRepository = paymentRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.orderOutboxHelper = orderOutboxHelper;
        this.paymentResponseMessagePublisher = paymentResponseMessagePublisher;
    }

    @Transactional
    public void persistPayment(PaymentRequest paymentRequest) {
        if (publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.COMPLETED)) {
            log.info("An outbox message with saga id: {} is already saved to database!", paymentRequest.getSagaId());
            return;
        }

        log.info("Received payment complete event for order id: {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
        
        // Strategy Pattern - find the right processor (default to WALLET for now)
        PaymentProcessorStrategy strategy = getStrategy("WALLET");

        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent = strategy.processPayment(payment, failureMessages);

        persistDbObjects(payment, paymentEvent, failureMessages);

        orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
                paymentEvent.getPayment().getPaymentStatus(),
                OutboxStatus.STARTED,
                UUID.fromString(paymentRequest.getSagaId()));
    }

    @Transactional
    public void persistCancelPayment(PaymentRequest paymentRequest) {
        if (publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.CANCELLED)) {
            log.info("An outbox message with saga id: {} is already saved to database!", paymentRequest.getSagaId());
            return;
        }

        log.info("Received payment rollback event for order id: {}", paymentRequest.getOrderId());
        Optional<Payment> paymentResponse = paymentRepository
                .findByOrderId(UUID.fromString(paymentRequest.getOrderId()));
        if (paymentResponse.isEmpty()) {
            log.error("Payment with order id: {} could not be found!", paymentRequest.getOrderId());
            throw new PaymentNotFoundException("Payment with order id: " +
                    paymentRequest.getOrderId() + " could not be found!");
        }
        Payment paymentDb = paymentResponse.get();

        // The database does not store restaurantId, so we recreate the Payment with the restaurantId from the cancel request
        Payment payment = Payment.builder()
                .paymentId(paymentDb.getId())
                .customerId(paymentDb.getCustomerId())
                .orderId(paymentDb.getOrderId())
                .price(paymentDb.getPrice())
                .paymentStatus(paymentDb.getPaymentStatus())
                .createdAt(paymentDb.getCreatedAt())
                .restaurantId(new com.berkay.domain.valueobject.RestaurantId(UUID.fromString(paymentRequest.getRestaurantId())))
                .build();

        PaymentProcessorStrategy strategy = getStrategy("WALLET");

        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent = strategy.refundPayment(payment, failureMessages);
        
        persistDbObjects(payment, paymentEvent, failureMessages);

        orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
                paymentEvent.getPayment().getPaymentStatus(),
                OutboxStatus.STARTED,
                UUID.fromString(paymentRequest.getSagaId()));
    }

    private PaymentProcessorStrategy getStrategy(String paymentMethod) {
        return paymentStrategies.stream()
                .filter(s -> s.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new PaymentApplicationServiceException("No strategy found for method: " + paymentMethod));
    }

    private void persistDbObjects(Payment payment,
                                  PaymentEvent paymentEvent,
                                  List<String> failureMessages) {
        paymentRepository.save(payment);

        if (failureMessages.isEmpty()) {
            if (paymentEvent.getCustomerWalletTransaction() != null && paymentEvent.getCustomerWallet() != null) {
                walletRepository.save(paymentEvent.getCustomerWallet());
                walletTransactionRepository.save(paymentEvent.getCustomerWalletTransaction());
            }
            if (paymentEvent.getRestaurantWalletTransaction() != null && paymentEvent.getRestaurantWallet() != null) {
                walletRepository.save(paymentEvent.getRestaurantWallet());
                walletTransactionRepository.save(paymentEvent.getRestaurantWalletTransaction());
            }
        }
    }

    private boolean publishIfOutboxMessageProcessedForPayment(PaymentRequest paymentRequest,
                                                              PaymentStatus paymentStatus){
        Optional<OrderOutboxMessage> orderOutboxMessage =
                orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(
                        UUID.fromString(paymentRequest.getSagaId()),
                        paymentStatus);
        if (orderOutboxMessage.isPresent()) {
            paymentResponseMessagePublisher.publish(orderOutboxMessage.get(), orderOutboxHelper::updateOutboxMessage);
            return true;
        }
        return false;
    }
}