package com.berkay.application.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDTO handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ErrorDTO.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Unexpected error!")
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleMethodArgumentTypeMismatchException(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException exception) {
        log.warn("Argument type mismatch: {}", exception.getMessage());
        return ErrorDTO.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Invalid parameter format for " + exception.getName())
                .build();
    }


    @ResponseBody
    @ExceptionHandler(value = {com.berkay.application.exception.TokenRevokedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public java.util.Map<String, Object> handleTokenRevokedException(com.berkay.application.exception.TokenRevokedException exception) {
        log.warn("Token revoked exception: {}", exception.getMessage());
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", 401);
        response.put("error_code", exception.getErrorCode());
        response.put("message", exception.getMessage());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(value = {com.berkay.application.exception.TokenExpiredException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public java.util.Map<String, Object> handleTokenExpiredException(com.berkay.application.exception.TokenExpiredException exception) {
        log.warn("Token expired exception: {}", exception.getMessage());
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", 401);
        response.put("error_code", exception.getErrorCode());
        response.put("message", exception.getMessage());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(value = {org.springframework.security.access.AccessDeniedException.class, org.springframework.security.authorization.AuthorizationDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorDTO handleAccessDeniedException(Exception exception) {
        log.warn("Access denied! Message: {}", exception.getMessage());
        return ErrorDTO.builder()
                .code(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Access Denied!")
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {com.berkay.application.exception.GlobalConflictException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDTO handleException(com.berkay.application.exception.GlobalConflictException exception) {
        log.error(exception.getMessage(), exception);
        return ErrorDTO.builder()
                .code(HttpStatus.CONFLICT.getReasonPhrase())
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {org.springframework.web.bind.MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleMissingServletRequestParameterException(org.springframework.web.bind.MissingServletRequestParameterException exception) {
        String message = "Required request parameter '" + exception.getParameterName() + "' for method parameter type " + exception.getParameterType() + " is not present";
        log.warn(message);
        return ErrorDTO.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleException(ValidationException validationException) { // These type of exceptions occurs when validation errors
        ErrorDTO errorDTO;
        if (validationException instanceof ConstraintViolationException) {
            String violations = extractViolationsFromException((ConstraintViolationException) validationException);
            log.error(violations, validationException);
            errorDTO = ErrorDTO.builder()
                    .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(violations)
                    .build();
        } else { // If exception is another validation exception
            String exceptionMessage = validationException.getMessage();
            log.error(exceptionMessage, validationException);
            errorDTO = ErrorDTO.builder()
                    .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(exceptionMessage)
                    .build();
        }
        return errorDTO;
    }

    @ResponseBody
    @ExceptionHandler(value = {org.springframework.web.bind.MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleException(org.springframework.web.bind.MethodArgumentNotValidException exception) {
        String violations = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("--"));
        log.error(violations, exception);
        return ErrorDTO.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(violations)
                .build();
    }

    private String extractViolationsFromException(ConstraintViolationException validationException) {
        return validationException.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("--"));
    }

}