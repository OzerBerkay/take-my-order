package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.event.OrderCancelledEvent;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.berkay.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.berkay.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.berkay.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.berkay.outbox.OutboxStatus;
import com.berkay.saga.SagaStatus;
import com.berkay.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.berkay.domain.DomainConstants.UTC;

@Slf4j
@Component
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {

    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderDataMapper orderDataMapper;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public OrderApprovalSaga(OrderDomainService orderDomainService,
                             OrderSagaHelper orderSagaHelper,
                             PaymentOutboxHelper paymentOutboxHelper,
                             ApprovalOutboxHelper approvalOutboxHelper,
                             OrderDataMapper orderDataMapper,
                             com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.orderDomainService = orderDomainService;
        this.orderSagaHelper = orderSagaHelper;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderDataMapper = orderDataMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void process(RestaurantApprovalResponse restaurantApprovalResponse) {
        Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
        if (order.getOrderStatus() != OrderStatus.PAID) {
            log.warn("Order with id: {} is already processed or moved past PAID state (current status: {}). Duplicate Kafka VALIDATED response ignored.",
                    order.getId().getValue(), order.getOrderStatus());
            return;
        }

        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse =
                approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(restaurantApprovalResponse.getSagaId()),
                        SagaStatus.PROCESSING);

        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed!",
                    restaurantApprovalResponse.getSagaId());
            return;
        }

        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();

        orderDomainService.waitForApproval(order);
        orderSagaHelper.saveOrder(order);

        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderApprovalOutboxMessage.setOrderStatus(OrderStatus.WAITING_FOR_APPROVAL);
        orderApprovalOutboxMessage.setSagaStatus(SagaStatus.PROCESSING);
        orderApprovalOutboxMessage.setOutboxStatus(OutboxStatus.COMPLETED);
        approvalOutboxHelper.save(orderApprovalOutboxMessage);

        log.info("Order with id: {} is in WAITING_FOR_APPROVAL state", order.getId().getValue());
    }

    @Override
    @Transactional
    public void rollback(RestaurantApprovalResponse restaurantApprovalResponse) {
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse =
                approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(restaurantApprovalResponse.getSagaId()),
                        SagaStatus.COMPENSATING, SagaStatus.PROCESSING);

        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            log.info("Approval outbox message with saga id: {} is already processed or compensated!",
                    restaurantApprovalResponse.getSagaId());
            return;
        }

        OrderApprovalOutboxMessage approvalOutboxMessage = orderApprovalOutboxMessageResponse.get();

        if (approvalOutboxMessage.getSagaStatus() == SagaStatus.PROCESSING) {
            // KOL 1: İlk oluşturma evresinde restoran otomatik ret yanıtı (Senaryo 1.2.B)
            Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
            if (order.getOrderStatus() != OrderStatus.PAID) {
                log.warn("Order with id: {} is already processed or moved past PAID state (current status: {}). Duplicate Kafka REJECTED response ignored.",
                        order.getId().getValue(), order.getOrderStatus());
                return;
            }

            com.berkay.domain.valueobject.CancelReason reason = restaurantApprovalResponse.getCancelReason() != null
                    ? com.berkay.domain.valueobject.CancelReason.valueOf(restaurantApprovalResponse.getCancelReason())
                    : com.berkay.domain.valueobject.CancelReason.AUTO_REJECTED_OUT_OF_STOCK;

            order.initCancel(reason, null, "Restoran otomatik ön doğrulamada siparişi reddetti.");
            orderSagaHelper.saveOrder(order);

            // AO_1'i yerel olarak COMPENSATED kapat (stok hiç düşmediği için Kafka mesajı atılmaz)
            approvalOutboxMessage.setOrderStatus(OrderStatus.CANCELLING);
            approvalOutboxMessage.setSagaStatus(SagaStatus.COMPENSATED);
            approvalOutboxMessage.setOutboxStatus(OutboxStatus.COMPLETED);
            approvalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
            approvalOutboxHelper.save(approvalOutboxMessage);

            // PO_1'i ödeme iadesi için COMPENSATING + STARTED olarak re-arm et
            Optional<OrderPaymentOutboxMessage> paymentOutboxOpt = paymentOutboxHelper
                    .getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(restaurantApprovalResponse.getSagaId()),
                            SagaStatus.PROCESSING);
            if (paymentOutboxOpt.isPresent()) {
                OrderPaymentOutboxMessage paymentOutboxMessage = paymentOutboxOpt.get();
                paymentOutboxMessage.setOrderStatus(OrderStatus.CANCELLING);
                paymentOutboxMessage.setSagaStatus(SagaStatus.COMPENSATING);
                paymentOutboxMessage.setOutboxStatus(OutboxStatus.STARTED);
                paymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
                paymentOutboxMessage.setPayload(createPaymentPayload(
                        orderDataMapper.orderToOrderPaymentEventPayload(order, com.berkay.domain.valueobject.PaymentOrderStatus.CANCELLED)));
                paymentOutboxHelper.save(paymentOutboxMessage);
            }
            log.info("Order with id: {} initiated auto-reject cancellation and payment refund re-armed.", order.getId().getValue());
        } else if (approvalOutboxMessage.getSagaStatus() == SagaStatus.COMPENSATING) {
            // KOL 2: Şefin manuel iptali sonrası stok iadesinin tamamlanması (Senaryo 1.2.C)
            approvalOutboxMessage.setSagaStatus(SagaStatus.COMPENSATED);
            approvalOutboxMessage.setOutboxStatus(OutboxStatus.COMPLETED);
            approvalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
            approvalOutboxHelper.save(approvalOutboxMessage);

            // Fork-Join Kontrolü: Ödeme iadesi de bitti mi (veya ödeme outbox hiç yok mu)?
            Optional<OrderPaymentOutboxMessage> paymentOutboxOpt = paymentOutboxHelper
                    .getPaymentOutboxMessageBySagaId(UUID.fromString(restaurantApprovalResponse.getSagaId()));

            boolean isPaymentCompensatedOrNotNeeded = paymentOutboxOpt.isEmpty()
                    || paymentOutboxOpt.get().getSagaStatus() == SagaStatus.COMPENSATED;

            if (isPaymentCompensatedOrNotNeeded) {
                Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
                order.cancel(order.getCancelReason());
                orderSagaHelper.saveOrder(order);

                // Nihai senkronizasyon: Her iki outbox'ı CANCELLED olarak mühürle
                approvalOutboxMessage.setOrderStatus(OrderStatus.CANCELLED);
                approvalOutboxHelper.save(approvalOutboxMessage);

                if (paymentOutboxOpt.isPresent()) {
                    OrderPaymentOutboxMessage paymentOutboxMessage = paymentOutboxOpt.get();
                    paymentOutboxMessage.setOrderStatus(OrderStatus.CANCELLED);
                    paymentOutboxHelper.save(paymentOutboxMessage);
                }
                log.info("Order with id: {} is finalized as CANCELLED (Fork-Join barrier complete).", order.getId().getValue());
            } else {
                log.info("Order with id: {} remains in CANCELLING state, awaiting payment compensation.",
                        restaurantApprovalResponse.getOrderId());
            }
        }
    }

    private String createPaymentPayload(com.berkay.order.service.domain.outbox.model.payment.OrderPaymentEventPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Could not create OrderPaymentEventPayload JSON for order id: {}", payload.getOrderId(), e);
            throw new OrderDomainException("Could not create OrderPaymentEventPayload JSON for order id: " + payload.getOrderId(), e);
        }
    }
}
