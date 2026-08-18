package com.berkay.payment.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.valueobject.WalletId;
import com.berkay.payment.service.domain.valueobject.WalletTransactionId;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class WalletTransaction extends BaseEntity<WalletTransactionId> {

    private final WalletId walletId;
    private final Money amount;
    private final WalletTransactionType transactionType;
    private final String referenceId;
    private final String idempotencyKey;
    private final ZonedDateTime createdAt;

    private WalletTransaction(Builder builder) {
        setId(builder.walletTransactionId);
        walletId = builder.walletId;
        amount = builder.amount;
        transactionType = builder.transactionType;
        referenceId = builder.referenceId;
        idempotencyKey = builder.idempotencyKey;
        createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public WalletId getWalletId() {
        return walletId;
    }

    public Money getAmount() {
        return amount;
    }

    public WalletTransactionType getTransactionType() {
        return transactionType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private WalletTransactionId walletTransactionId;
        private WalletId walletId;
        private Money amount;
        private WalletTransactionType transactionType;
        private String referenceId;
        private String idempotencyKey;
        private ZonedDateTime createdAt;

        private Builder() {
        }

        public Builder walletTransactionId(WalletTransactionId val) {
            walletTransactionId = val;
            return this;
        }

        public Builder walletId(WalletId val) {
            walletId = val;
            return this;
        }

        public Builder amount(Money val) {
            amount = val;
            return this;
        }

        public Builder transactionType(WalletTransactionType val) {
            transactionType = val;
            return this;
        }

        public Builder referenceId(String val) {
            referenceId = val;
            return this;
        }

        public Builder idempotencyKey(String val) {
            idempotencyKey = val;
            return this;
        }

        public Builder createdAt(ZonedDateTime val) {
            createdAt = val;
            return this;
        }

        public WalletTransaction build() {
            if (walletTransactionId == null) {
                walletTransactionId = new WalletTransactionId(UUID.randomUUID());
            }
            if (createdAt == null) {
                createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
            }
            return new WalletTransaction(this);
        }
    }
}
