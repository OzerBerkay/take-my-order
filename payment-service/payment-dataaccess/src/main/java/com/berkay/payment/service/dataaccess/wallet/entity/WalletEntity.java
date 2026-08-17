package com.berkay.payment.service.dataaccess.wallet.entity;

import com.berkay.payment.service.domain.valueobject.OwnerType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallets")
@Entity
public class WalletEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id", unique = true, nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OwnerType ownerType;

    @Column(nullable = false)
    private BigDecimal balance;

    @Version
    private Integer version;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WalletTransactionEntity> transactions;
}
