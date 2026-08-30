package com.berkay.identity.service.dto.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterCustomerCommandValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid Phone Number Should Pass Validation")
    void validPhoneNumber_ShouldPass() {
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email("test@test.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+905551234567")
                .build();

        Set<ConstraintViolation<RegisterCustomerCommand>> violations = validator.validate(command);
        assertTrue(violations.isEmpty(), "Valid command should have no violations");
    }

    @Test
    @DisplayName("Invalid Phone Number (Missing +90) Should Fail Validation")
    void invalidPhoneNumber_MissingPlus90_ShouldFail() {
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email("test@test.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("05551234567")
                .build();

        Set<ConstraintViolation<RegisterCustomerCommand>> violations = validator.validateProperty(command, "phoneNumber");
        assertFalse(violations.isEmpty(), "Phone number without +90 should fail");
        assertEquals("Invalid phone number format", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Invalid Phone Number (Wrong Length) Should Fail Validation")
    void invalidPhoneNumber_WrongLength_ShouldFail() {
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email("test@test.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+90555123456") // Missing one digit
                .build();

        Set<ConstraintViolation<RegisterCustomerCommand>> violations = validator.validateProperty(command, "phoneNumber");
        assertFalse(violations.isEmpty(), "Phone number with 12 characters should fail");
        assertEquals("Invalid phone number format", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Invalid Phone Number (Contains Letters) Should Fail Validation")
    void invalidPhoneNumber_ContainsLetters_ShouldFail() {
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email("test@test.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+9055512345ab")
                .build();

        Set<ConstraintViolation<RegisterCustomerCommand>> violations = validator.validateProperty(command, "phoneNumber");
        assertFalse(violations.isEmpty(), "Phone number with letters should fail");
        assertEquals("Invalid phone number format", violations.iterator().next().getMessage());
    }
}
