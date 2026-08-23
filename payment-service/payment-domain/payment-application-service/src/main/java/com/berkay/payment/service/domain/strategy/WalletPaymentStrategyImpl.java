package com.berkay.payment.service.domain.strategy;

import com.berkay.payment.service.domain.PaymentDomainService;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.event.PaymentEvent;
import com.berkay.payment.service.domain.exception.PaymentApplicationServiceException;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class WalletPaymentStrategyImpl implements PaymentProcessorStrategy {

    private final WalletRepository walletRepository;
    private final PaymentDomainService paymentDomainService;

    public WalletPaymentStrategyImpl(WalletRepository walletRepository,
                                     PaymentDomainService paymentDomainService) {
        this.walletRepository = walletRepository;
        this.paymentDomainService = paymentDomainService;
    }

    @Override
    public PaymentEvent processPayment(Payment payment, List<String> failureMessages) {
        Wallet customerWallet = getWallet(payment.getCustomerId().getValue(), "customer");
        Wallet restaurantWallet = getWallet(payment.getRestaurantId().getValue(), "restaurant");
        return paymentDomainService.validateAndInitiatePayment(payment, customerWallet, restaurantWallet, failureMessages);
    }

    @Override
    public PaymentEvent refundPayment(Payment payment, List<String> failureMessages) {
        Wallet customerWallet = getWallet(payment.getCustomerId().getValue(), "customer");
        Wallet restaurantWallet = getWallet(payment.getRestaurantId().getValue(), "restaurant");
        return paymentDomainService.validateAndCancelPayment(payment, customerWallet, restaurantWallet, failureMessages);
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
