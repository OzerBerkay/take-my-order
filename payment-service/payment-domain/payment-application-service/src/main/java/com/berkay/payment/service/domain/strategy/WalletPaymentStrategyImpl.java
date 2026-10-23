package com.berkay.payment.service.domain.strategy;

import com.berkay.payment.service.domain.PaymentDomainService;
import com.berkay.payment.service.domain.entity.OrderEscrow;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.event.PaymentEvent;
import com.berkay.payment.service.domain.exception.PaymentApplicationServiceException;
import com.berkay.payment.service.domain.ports.output.repository.OrderEscrowRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class WalletPaymentStrategyImpl implements PaymentProcessorStrategy {

    private final WalletRepository walletRepository;
    private final OrderEscrowRepository orderEscrowRepository;
    private final PaymentDomainService paymentDomainService;

    public WalletPaymentStrategyImpl(WalletRepository walletRepository,
                                     OrderEscrowRepository orderEscrowRepository,
                                     PaymentDomainService paymentDomainService) {
        this.walletRepository = walletRepository;
        this.orderEscrowRepository = orderEscrowRepository;
        this.paymentDomainService = paymentDomainService;
    }

    @Override
    public PaymentEvent processPayment(Payment payment, List<String> failureMessages) {
        Wallet customerWallet = getWallet(payment.getCustomerId().getValue(), "customer");
        return paymentDomainService.validateAndInitiatePayment(payment, customerWallet, failureMessages);
    }

    @Override
    public PaymentEvent refundPayment(Payment payment, List<String> failureMessages) {
        Wallet customerWallet = getWallet(payment.getCustomerId().getValue(), "customer");
        Optional<OrderEscrow> orderEscrow = orderEscrowRepository.findByOrderId(payment.getOrderId());
        if (orderEscrow.isPresent()) {
            return paymentDomainService.validateAndCancelPayment(payment, customerWallet, orderEscrow.get(), failureMessages);
        }
        // Fallback for legacy orders created before OrderEscrow table:
        if (payment.getRestaurantId() != null) {
            Optional<Wallet> restaurantWalletOpt = walletRepository.findByOwnerId(payment.getRestaurantId().getValue());
            if (restaurantWalletOpt.isPresent()) {
                log.info("OrderEscrow not found for order id: {}. Falling back to direct restaurant wallet refund.",
                        payment.getOrderId().getValue());
                return paymentDomainService.validateAndCancelPayment(payment, customerWallet, restaurantWalletOpt.get(), failureMessages);
            }
        }
        return paymentDomainService.validateAndCancelPayment(payment, customerWallet, (OrderEscrow) null, failureMessages);
    }

    @Override
    public boolean supports(String paymentMethod) {
        return "WALLET".equalsIgnoreCase(paymentMethod);
    }

    private Wallet getWallet(java.util.UUID ownerId, String ownerType) {
        Optional<Wallet> walletResult = walletRepository.findByOwnerId(ownerId);
        if (walletResult.isEmpty()) {
            log.error("Could not find wallet for {} id: {}", ownerType, ownerId);
            throw new PaymentApplicationServiceException("Could not find wallet for " + ownerType + " id: " + ownerId);
        }
        return walletResult.get();
    }
}
