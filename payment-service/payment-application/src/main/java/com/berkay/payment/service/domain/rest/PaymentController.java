package com.berkay.payment.service.domain.rest;

import com.berkay.payment.service.domain.dto.create.CreditOperationCommand;
import com.berkay.payment.service.domain.dto.create.CreditOperationResponse;
import com.berkay.payment.service.domain.dto.create.CreditOperationRequest;
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
}
