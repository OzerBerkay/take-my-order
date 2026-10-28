package com.berkay.order.service.application.exception.handler;

import com.berkay.application.handler.ErrorDTO;
import com.berkay.application.handler.GlobalExceptionHandler;
import com.berkay.order.service.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class OrderGlobalExceptionHandler extends GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {OrderNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleException(OrderNotFoundException orderNotFoundException) {
        log.warn("Order not found: {}", orderNotFoundException.getMessage());
        return ErrorDTO.builder()
                .code("ORDER_NOT_FOUND")
                .errorCode("ORDER_NOT_FOUND")
                .message(orderNotFoundException.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {ReviewNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleReviewNotFoundException(ReviewNotFoundException exception) {
        log.warn("Review not found: {}", exception.getMessage());
        return ErrorDTO.builder()
                .code("REVIEW_NOT_FOUND")
                .errorCode("REVIEW_NOT_FOUND")
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {ReviewAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDTO handleReviewAlreadyExistsException(ReviewAlreadyExistsException exception) {
        log.warn("Review already exists: {}", exception.getMessage());
        return ErrorDTO.builder()
                .code("REVIEW_ALREADY_EXISTS")
                .errorCode("REVIEW_ALREADY_EXISTS")
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {
            OrderConflictException.class,
            org.springframework.dao.OptimisticLockingFailureException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDTO handleConflictException(Exception exception) {
        log.warn("Conflict detected: {}", exception.getMessage());
        return ErrorDTO.builder()
                .code("ORDER_CONFLICT")
                .errorCode("ORDER_CONFLICT")
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {OrderNotDeliveredException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleOrderNotDeliveredException(OrderNotDeliveredException exception) {
        log.warn("Order not delivered for review: {}", exception.getMessage());
        return ErrorDTO.builder()
                .code("ORDER_NOT_DELIVERED")
                .errorCode("ORDER_NOT_DELIVERED")
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {ReviewTimeExpiredException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleReviewTimeExpiredException(ReviewTimeExpiredException exception) {
        log.warn("Review time expired: {}", exception.getMessage());
        return ErrorDTO.builder()
                .code("REVIEW_TIME_EXPIRED")
                .errorCode("REVIEW_TIME_EXPIRED")
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {ReviewAlreadyDeactivatedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleReviewAlreadyDeactivatedException(ReviewAlreadyDeactivatedException exception) {
        log.warn("Review already deactivated: {}", exception.getMessage());
        return ErrorDTO.builder()
                .code("REVIEW_ALREADY_DEACTIVATED")
                .errorCode("REVIEW_ALREADY_DEACTIVATED")
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {OrderDomainException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleException(OrderDomainException orderDomainException) {
        log.warn("Order domain error: {}", orderDomainException.getMessage());
        String code = orderDomainException.getErrorCode() != null
                ? orderDomainException.getErrorCode()
                : "ORDER_DOMAIN_ERROR";
        return ErrorDTO.builder()
                .code(code)
                .errorCode(code)
                .message(orderDomainException.getMessage())
                .build();
    }
}