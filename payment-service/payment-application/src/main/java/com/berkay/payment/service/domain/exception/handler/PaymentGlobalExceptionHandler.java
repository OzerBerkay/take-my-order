package com.berkay.payment.service.domain.exception.handler;

import com.berkay.application.handler.ErrorDTO;
import com.berkay.application.handler.GlobalExceptionHandler;
import com.berkay.payment.service.domain.exception.PaymentApplicationServiceException;
import com.berkay.payment.service.domain.exception.PaymentDomainException;
import com.berkay.payment.service.domain.exception.PaymentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class PaymentGlobalExceptionHandler extends GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {PaymentDomainException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleException(PaymentDomainException paymentDomainException) {
        log.error(paymentDomainException.getMessage(), paymentDomainException);
        return ErrorDTO.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(paymentDomainException.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {PaymentNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleException(PaymentNotFoundException paymentNotFoundException) {
        log.error(paymentNotFoundException.getMessage(), paymentNotFoundException);
        return ErrorDTO.builder()
                .code(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(paymentNotFoundException.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {PaymentApplicationServiceException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleException(PaymentApplicationServiceException paymentApplicationServiceException) {
        log.error(paymentApplicationServiceException.getMessage(), paymentApplicationServiceException);
        return ErrorDTO.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(paymentApplicationServiceException.getMessage())
                .build();
    }
}
