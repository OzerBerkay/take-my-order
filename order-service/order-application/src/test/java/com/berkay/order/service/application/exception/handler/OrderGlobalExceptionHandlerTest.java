package com.berkay.order.service.application.exception.handler;

import com.berkay.application.handler.ErrorDTO;
import com.berkay.order.service.domain.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderGlobalExceptionHandlerTest {

    private OrderGlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new OrderGlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should return ORDER_NOT_DELIVERED for OrderNotDeliveredException")
    void testOrderNotDeliveredException() {
        OrderNotDeliveredException ex = new OrderNotDeliveredException("Order is in delivery");
        ErrorDTO dto = exceptionHandler.handleOrderNotDeliveredException(ex);

        assertNotNull(dto);
        assertEquals("ORDER_NOT_DELIVERED", dto.getCode());
        assertEquals("ORDER_NOT_DELIVERED", dto.getErrorCode());
        assertEquals("Order is in delivery", dto.getMessage());
    }

    @Test
    @DisplayName("Should return REVIEW_TIME_EXPIRED for ReviewTimeExpiredException")
    void testReviewTimeExpiredException() {
        ReviewTimeExpiredException ex = new ReviewTimeExpiredException("7 days expired");
        ErrorDTO dto = exceptionHandler.handleReviewTimeExpiredException(ex);

        assertNotNull(dto);
        assertEquals("REVIEW_TIME_EXPIRED", dto.getCode());
        assertEquals("REVIEW_TIME_EXPIRED", dto.getErrorCode());
        assertEquals("7 days expired", dto.getMessage());
    }

    @Test
    @DisplayName("Should return REVIEW_ALREADY_EXISTS for ReviewAlreadyExistsException")
    void testReviewAlreadyExistsException() {
        ReviewAlreadyExistsException ex = new ReviewAlreadyExistsException("Review already made");
        ErrorDTO dto = exceptionHandler.handleReviewAlreadyExistsException(ex);

        assertNotNull(dto);
        assertEquals("REVIEW_ALREADY_EXISTS", dto.getCode());
        assertEquals("REVIEW_ALREADY_EXISTS", dto.getErrorCode());
        assertEquals("Review already made", dto.getMessage());
    }

    @Test
    @DisplayName("Should return REVIEW_NOT_FOUND for ReviewNotFoundException")
    void testReviewNotFoundException() {
        ReviewNotFoundException ex = new ReviewNotFoundException("Review not found");
        ErrorDTO dto = exceptionHandler.handleReviewNotFoundException(ex);

        assertNotNull(dto);
        assertEquals("REVIEW_NOT_FOUND", dto.getCode());
        assertEquals("REVIEW_NOT_FOUND", dto.getErrorCode());
        assertEquals("Review not found", dto.getMessage());
    }

    @Test
    @DisplayName("Should return REVIEW_ALREADY_DEACTIVATED for ReviewAlreadyDeactivatedException")
    void testReviewAlreadyDeactivatedException() {
        ReviewAlreadyDeactivatedException ex = new ReviewAlreadyDeactivatedException("Review deactivated");
        ErrorDTO dto = exceptionHandler.handleReviewAlreadyDeactivatedException(ex);

        assertNotNull(dto);
        assertEquals("REVIEW_ALREADY_DEACTIVATED", dto.getCode());
        assertEquals("REVIEW_ALREADY_DEACTIVATED", dto.getErrorCode());
        assertEquals("Review deactivated", dto.getMessage());
    }

    @Test
    @DisplayName("Should return ORDER_NOT_FOUND for OrderNotFoundException")
    void testOrderNotFoundException() {
        OrderNotFoundException ex = new OrderNotFoundException("Order not found");
        ErrorDTO dto = exceptionHandler.handleException(ex);

        assertNotNull(dto);
        assertEquals("ORDER_NOT_FOUND", dto.getCode());
        assertEquals("ORDER_NOT_FOUND", dto.getErrorCode());
        assertEquals("Order not found", dto.getMessage());
    }

    @Test
    @DisplayName("Should return ORDER_CONFLICT for OrderConflictException")
    void testOrderConflictException() {
        OrderConflictException ex = new OrderConflictException("Conflict detected");
        ErrorDTO dto = exceptionHandler.handleConflictException(ex);

        assertNotNull(dto);
        assertEquals("ORDER_CONFLICT", dto.getCode());
        assertEquals("ORDER_CONFLICT", dto.getErrorCode());
        assertEquals("Conflict detected", dto.getMessage());
    }

    @Test
    @DisplayName("Should return specific error code for OrderDomainException when errorCode is provided")
    void testOrderDomainExceptionWithErrorCode() {
        OrderDomainException ex = new OrderDomainException("REVIEW_ALREADY_REPLIED", "Review has already been replied to");
        ErrorDTO dto = exceptionHandler.handleException(ex);

        assertNotNull(dto);
        assertEquals("REVIEW_ALREADY_REPLIED", dto.getCode());
        assertEquals("REVIEW_ALREADY_REPLIED", dto.getErrorCode());
        assertEquals("Review has already been replied to", dto.getMessage());
    }

    @Test
    @DisplayName("Should return ORDER_DOMAIN_ERROR for OrderDomainException when no errorCode provided")
    void testOrderDomainExceptionWithoutErrorCode() {
        OrderDomainException ex = new OrderDomainException("Generic domain error");
        ErrorDTO dto = exceptionHandler.handleException(ex);

        assertNotNull(dto);
        assertEquals("ORDER_DOMAIN_ERROR", dto.getCode());
        assertEquals("ORDER_DOMAIN_ERROR", dto.getErrorCode());
        assertEquals("Generic domain error", dto.getMessage());
    }

    @Test
    @DisplayName("Should return ACCESS_DENIED for AccessDeniedException")
    void testAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden access");
        ErrorDTO dto = exceptionHandler.handleAccessDeniedException(ex);

        assertNotNull(dto);
        assertEquals("ACCESS_DENIED", dto.getCode());
        assertEquals("ACCESS_DENIED", dto.getErrorCode());
        assertEquals("Access Denied!", dto.getMessage());
    }
}
