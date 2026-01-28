package com.berkay.payment.service.domain.rest;

import com.berkay.payment.service.domain.dto.common.PagedResponse;
import com.berkay.payment.service.domain.dto.create.CreditOperationCommand;
import com.berkay.payment.service.domain.dto.create.CreditOperationResponse;
import com.berkay.payment.service.domain.dto.create.CreditOperationRequest;
import com.berkay.payment.service.domain.dto.query.CreditBalanceResponse;
import com.berkay.payment.service.domain.dto.query.CreditHistoryResponse;
import com.berkay.payment.service.domain.dto.query.GetCreditBalanceQuery;
import com.berkay.payment.service.domain.dto.query.GetCreditHistoryQuery;
import com.berkay.payment.service.domain.mapper.PaymentRequestMapper;
import com.berkay.payment.service.domain.ports.input.service.PaymentApplicationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/payments", produces = "application/vnd.api.v1+json")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;
    private final PaymentRequestMapper paymentRequestMapper;

    public PaymentController(PaymentApplicationService paymentApplicationService,
                             PaymentRequestMapper paymentRequestMapper) {
        this.paymentApplicationService = paymentApplicationService;
        this.paymentRequestMapper = paymentRequestMapper;
    }

    @PostMapping("/{customerId}/operations")
    public ResponseEntity<CreditOperationResponse> createCreditOperation(@PathVariable UUID customerId,
                                                                         @RequestBody @Valid CreditOperationRequest creditOperationRequest) {
        log.info("Creating credit operation for customer id: {}, amount: {}, type: {}",
                customerId, creditOperationRequest.getAmount(), creditOperationRequest.getTransactionType().name());

        CreditOperationCommand command = paymentRequestMapper
                .creditOperationRequestToCreditOperationCommand(customerId, creditOperationRequest);

        CreditOperationResponse response = paymentApplicationService.processCreditOperation(command);
        log.info("Credit operation processed for customer id: {}, new balance: {}",
                response.getCustomerId(), response.getNewBalance());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}/balance")
    public ResponseEntity<CreditBalanceResponse> getCreditBalance(@PathVariable UUID customerId) {
        log.info("Querying credit balance for customer id: {}", customerId);

        CreditBalanceResponse response =
                paymentApplicationService.getCreditBalance(new GetCreditBalanceQuery(customerId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}/history")
    public ResponseEntity<PagedResponse<CreditHistoryResponse>> getCreditHistory(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Received credit history query for customer: {}, page: {}, size: {}", customerId, page, size);

        GetCreditHistoryQuery query = new GetCreditHistoryQuery(customerId, page, size);

        PagedResponse<CreditHistoryResponse> response = paymentApplicationService.getCreditHistory(query);

        return ResponseEntity.ok(response);
    }
}
