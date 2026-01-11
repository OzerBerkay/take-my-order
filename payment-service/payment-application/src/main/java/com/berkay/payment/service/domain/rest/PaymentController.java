package com.berkay.payment.service.domain.rest;

import com.berkay.payment.service.domain.UpdateCreditCommandHandler;
import com.berkay.payment.service.domain.dto.UpdateCreditCommand;
import com.berkay.payment.service.domain.dto.UpdateCreditResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/payments")
public class PaymentController {

    private final UpdateCreditCommandHandler updateCreditCommandHandler;

    public PaymentController(UpdateCreditCommandHandler updateCreditCommandHandler) {
        this.updateCreditCommandHandler = updateCreditCommandHandler;
    }

    @PostMapping("/credit")
    public ResponseEntity<UpdateCreditResponse> updateCredit(@RequestBody @Valid UpdateCreditCommand updateCreditCommand) {
        log.info("Updating credit for customer id: {}", updateCreditCommand.getCustomerId());

        UpdateCreditResponse response = updateCreditCommandHandler.updateCredit(updateCreditCommand);

        log.info("Credit updated for customer id: {}, new balance: {}",
                response.getCustomerId(), response.getNewBalance());

        return ResponseEntity.ok(response);
    }
}
