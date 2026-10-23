package com.berkay.payment.service.dataaccess.wallet.adapter;

import com.berkay.payment.service.dataaccess.wallet.entity.WalletTransactionEntity;
import com.berkay.payment.service.dataaccess.wallet.mapper.WalletDataAccessMapper;
import com.berkay.payment.service.dataaccess.wallet.repository.WalletJpaRepository;
import com.berkay.payment.service.dataaccess.wallet.repository.WalletTransactionJpaRepository;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.valueobject.DomainPagedResult;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletTransactionRepositoryImplTest {

    @Mock
    private WalletTransactionJpaRepository walletTransactionJpaRepository;

    @Mock
    private WalletDataAccessMapper walletDataAccessMapper;

    @Mock
    private WalletJpaRepository walletJpaRepository;

    @InjectMocks
    private WalletTransactionRepositoryImpl repository;

    private final UUID WALLET_ID = UUID.randomUUID();

    @Test
    void testFindByWalletIdPaged() {
        WalletTransactionEntity entity = new WalletTransactionEntity();
        WalletTransaction domain = WalletTransaction.builder().build();

        PageImpl<WalletTransactionEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(walletTransactionJpaRepository.findByWalletIdOrderByCreatedAtDesc(eq(WALLET_ID), any()))
                .thenReturn(page);
        when(walletDataAccessMapper.walletTransactionEntityToWalletTransaction(entity))
                .thenReturn(domain);

        DomainPagedResult<WalletTransaction> result = repository.findByWalletIdPaged(WALLET_ID, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalElements());
    }
}
