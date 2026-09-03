package com.berkay.identity.service.application.exception.handler;

import com.berkay.application.handler.ErrorDTO;
import com.berkay.application.handler.GlobalExceptionHandler;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.exception.TokenRevokedDomainException;
import com.berkay.identity.service.domain.exception.UserNotFoundException;
import com.berkay.identity.service.domain.exception.UserAlreadyExistsException;
import com.berkay.identity.service.domain.exception.InvalidCredentialsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class IdentityGlobalExceptionHandler extends GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {UserAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleException(UserAlreadyExistsException exception) {
        log.warn(exception.getMessage());
        return ErrorDTO.builder()
                .code(exception.getErrorCode())
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {InvalidCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorDTO handleException(InvalidCredentialsException exception) {
        log.warn(exception.getMessage());
        return ErrorDTO.builder()
                .code(exception.getErrorCode())
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {IdentityDomainException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleException(IdentityDomainException exception) {
        log.warn(exception.getMessage());
        return ErrorDTO.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {UserNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleException(UserNotFoundException exception) {
        log.warn(exception.getMessage());
        return ErrorDTO.builder()
                .code(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {TokenRevokedDomainException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public java.util.Map<String, Object> handleTokenRevokedDomainException(TokenRevokedDomainException exception) {
        log.warn("Token revoked exception: {}", exception.getMessage());
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", 401);
        response.put("error_code", exception.getErrorCode());
        response.put("message", exception.getMessage());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(value = {com.berkay.identity.service.domain.exception.TokenExpiredDomainException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public java.util.Map<String, Object> handleTokenExpiredDomainException(com.berkay.identity.service.domain.exception.TokenExpiredDomainException exception) {
        log.warn("Token expired exception: {}", exception.getMessage());
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", 401);
        response.put("error_code", exception.getErrorCode());
        response.put("message", exception.getMessage());
        return response;
    }
}