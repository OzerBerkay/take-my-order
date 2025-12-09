package com.berkay.customer.service.domain.valueobject;

import com.berkay.domain.valueobject.BaseId;

import java.util.regex.Pattern;

public class CustomerEmail extends BaseId<String> {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    public CustomerEmail(String value) {
        super(value);
        validateEmail(value);
    }

    private void validateEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null!");
        }
        if (!PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email is not valid: " + email);
        }
    }
}