package com.berkay.identity.service.domain.valueobject;

import com.berkay.identity.service.domain.exception.IdentityDomainException;
import java.util.Objects;
import java.util.regex.Pattern;

public class PhoneNumber {
    private static final String PHONE_REGEX = "^\\+[1-9]\\d{1,14}$";
    private static final Pattern PATTERN = Pattern.compile(PHONE_REGEX);

    private final String value;

    public PhoneNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IdentityDomainException("Phone number cannot be empty!");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IdentityDomainException("Invalid phone number format! Use E.164 format (e.g. +905...)");
        }
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}