package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.payment.service.domain.dto.OrderDeliveredRequest;
import com.berkay.payment.service.domain.entity.OrderEscrow;
import com.berkay.payment.service.domain.entity.RestaurantWallet;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.ports.input.message.listener.OrderDeliveredMessageListener;
import com.berkay.payment.service.domain.ports.output.repository.OrderEscrowRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletTransactionRepository;
import com.berkay.payment.service.domain.valueobject.OrderEscrowStatus;
import com.berkay.payment.service.domain.valueobject.WalletId;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.berkay.domain.DomainConstants.UTC;

@Slf4j
@Service
public class OrderDeliveredMessageListenerImpl implements OrderDeliveredMessageListener {

    private final OrderEscrowRepository orderEscrowRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public OrderDeliveredMessageListenerImpl(OrderEscrowRepository orderEscrowRepository,
                                            WalletRepository walletRepository,
                                            WalletTransactionRepository walletTransactionRepository) {
        this.orderEscrowRepository = orderEscrowRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Override
    @Transactional
    public void settleOrder(OrderDeliveredRequest request) {
        log.info("Processing order delivered settlement for order id: {}", request.getOrderId());
        UUID orderIdUuid = UUID.fromString(request.getOrderId());
        UUID restaurantIdUuid = UUID.fromString(request.getRestaurantId());

        Optional<OrderEscrow> escrowOpt = orderEscrowRepository.findByOrderIdWithLock(new OrderId(orderIdUuid));
        if (escrowOpt.isEmpty()) {
            log.warn("Order escrow not found for order id: {}, skipping settlement", request.getOrderId());
            return;
        }

        OrderEscrow escrow = escrowOpt.get();
        if (escrow.getStatus() == OrderEscrowStatus.RELEASED) {
            log.info("Order escrow already released for order id: {}, idempotent skip", request.getOrderId());
            return;
        }

        if (escrow.getStatus() == OrderEscrowStatus.REFUNDED) {
            log.warn("Order escrow already refunded for order id: {}, cannot release", request.getOrderId());
            return;
        }

        escrow.release();
        orderEscrowRepository.save(escrow);

        Wallet wallet = walletRepository.findByOwnerIdWithLock(restaurantIdUuid)
                .orElseGet(() -> {
                    log.info("Creating wallet for restaurant id: {}", restaurantIdUuid);
                    RestaurantWallet newWallet = new RestaurantWallet(
                            new WalletId(UUID.randomUUID()),
                            restaurantIdUuid,
                            Money.ZERO
                    );
                    return walletRepository.save(newWallet);
                });

        wallet.addBalance(escrow.getAmount());
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .amount(escrow.getAmount())
                .transactionType(WalletTransactionType.ORDER_PAYOUT)
                .referenceId(request.getOrderId())
                .createdAt(ZonedDateTime.now(ZoneId.of(UTC)))
                .build();
        walletTransactionRepository.save(transaction);

        log.info("Settled order id: {} to restaurant id: {} with amount: {}",
                request.getOrderId(), restaurantIdUuid, escrow.getAmount().getAmount());
    }
}
