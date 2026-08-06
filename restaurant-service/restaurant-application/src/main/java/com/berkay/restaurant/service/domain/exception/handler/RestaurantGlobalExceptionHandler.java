package com.berkay.restaurant.service.domain.exception.handler;

import com.berkay.application.handler.GlobalExceptionHandler;
import com.berkay.application.handler.ErrorDTO;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class RestaurantGlobalExceptionHandler extends GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {RestaurantDomainException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleException(RestaurantDomainException restaurantDomainException) {
        log.warn(restaurantDomainException.getMessage());
        return ErrorDTO.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(restaurantDomainException.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {RestaurantNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleException(RestaurantNotFoundException restaurantNotFoundException) {
        log.warn(restaurantNotFoundException.getMessage());
        return ErrorDTO.builder()
                .code(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(restaurantNotFoundException.getMessage())
                .build();
    }
}