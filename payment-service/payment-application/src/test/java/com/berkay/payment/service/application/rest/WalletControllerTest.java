package com.berkay.payment.service.application.rest;

import com.berkay.payment.service.domain.WalletApplicationService;
import com.berkay.payment.service.domain.dto.wallet.RestaurantFinancialSummaryResponse;
import com.berkay.payment.service.domain.dto.wallet.WalletBalanceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletApplicationService walletApplicationService;

    @InjectMocks
    private WalletController walletController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();
    }

    @Test
    @DisplayName("GET /wallets/{ownerId}/financial-summary returns 200 OK with correct payload")
    void testGetFinancialSummary() throws Exception {
        UUID ownerId = UUID.randomUUID();
        RestaurantFinancialSummaryResponse response = RestaurantFinancialSummaryResponse.builder()
                .restaurantId(ownerId)
                .withdrawableBalance(new BigDecimal("1500.00"))
                .dailySettledRevenue(new BigDecimal("800.00"))
                .pendingPayout(new BigDecimal("350.00"))
                .dailyRefundedAmount(new BigDecimal("50.00"))
                .build();

        when(walletApplicationService.getFinancialSummary(ownerId)).thenReturn(response);

        mockMvc.perform(get("/wallets/{ownerId}/financial-summary", ownerId)
                        .accept("application/vnd.api.v1+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(ownerId.toString()))
                .andExpect(jsonPath("$.withdrawableBalance").value(1500.00))
                .andExpect(jsonPath("$.dailySettledRevenue").value(800.00))
                .andExpect(jsonPath("$.pendingPayout").value(350.00))
                .andExpect(jsonPath("$.dailyRefundedAmount").value(50.00));

        verify(walletApplicationService).getFinancialSummary(ownerId);
    }

    @Test
    @DisplayName("GET /wallets/{ownerId}/balance returns 200 OK")
    void testGetBalance() throws Exception {
        UUID ownerId = UUID.randomUUID();
        WalletBalanceResponse response = WalletBalanceResponse.builder()
                .ownerId(ownerId)
                .balance(new BigDecimal("500.00"))
                .build();

        when(walletApplicationService.getBalance(ownerId)).thenReturn(response);

        mockMvc.perform(get("/wallets/{ownerId}/balance", ownerId)
                        .accept("application/vnd.api.v1+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.balance").value(500.00));

        verify(walletApplicationService).getBalance(ownerId);
    }

    @Test
    @DisplayName("GET /wallets/{ownerId}/history returns 200 OK with paginated transactions")
    void testGetHistory() throws Exception {
        UUID ownerId = UUID.randomUUID();
        com.berkay.payment.service.domain.dto.wallet.WalletTransactionDto dto =
                com.berkay.payment.service.domain.dto.wallet.WalletTransactionDto.builder()
                        .amount(new BigDecimal("120.00"))
                        .transactionType(com.berkay.payment.service.domain.valueobject.WalletTransactionType.PAYMENT)
                        .referenceId("order-ref-999")
                        .createdAt(java.time.ZonedDateTime.now())
                        .build();

        com.berkay.payment.service.domain.dto.wallet.WalletHistoryResponse response =
                com.berkay.payment.service.domain.dto.wallet.WalletHistoryResponse.builder()
                        .ownerId(ownerId)
                        .content(java.util.List.of(dto))
                        .page(0)
                        .size(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build();

        when(walletApplicationService.getHistory(eq(ownerId), eq(com.berkay.payment.service.domain.valueobject.WalletTransactionType.PAYMENT), eq(0), eq(10)))
                .thenReturn(response);

        mockMvc.perform(get("/wallets/{ownerId}/history", ownerId)
                        .param("type", "PAYMENT")
                        .param("page", "0")
                        .param("size", "10")
                        .accept("application/vnd.api.v1+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.content[0].amount").value(120.00))
                .andExpect(jsonPath("$.content[0].transactionType").value("PAYMENT"))
                .andExpect(jsonPath("$.content[0].referenceId").value("order-ref-999"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(walletApplicationService).getHistory(ownerId, com.berkay.payment.service.domain.valueobject.WalletTransactionType.PAYMENT, 0, 10);
    }
}
