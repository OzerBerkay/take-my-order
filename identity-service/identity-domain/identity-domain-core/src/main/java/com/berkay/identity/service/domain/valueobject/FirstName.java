package com.berkay.identity.service.domain.valueobject;

import com.berkay.identity.service.domain.exception.IdentityDomainException;
import java.util.Objects;

public class FirstName {
    private final String value;

    public FirstName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IdentityDomainException("First name cannot be empty!");
        }
        if (value.length() > 50) {
            throw new IdentityDomainException("First name cannot exceed 50 characters!");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FirstName firstName = (FirstName) o;
        return Objects.equals(value, firstName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}