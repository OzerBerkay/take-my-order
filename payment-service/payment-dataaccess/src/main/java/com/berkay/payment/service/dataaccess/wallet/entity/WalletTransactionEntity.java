package com.berkay.payment.service.dataaccess.wallet.entity;

import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet_transactions")
@Entity
public class WalletTransactionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", referencedColumnName = "id", nullable = false)
    private WalletEntity wallet;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private WalletTransactionType transactionType;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
}
