package com.berkay.identity.service.dataaccess.role.exception;

public class RoleDataaccessException extends RuntimeException {
    public RoleDataaccessException(String message) {
        super(message);
    }

    public RoleDataaccessException(String message, Throwable cause) {
        super(message, cause);
    }
}