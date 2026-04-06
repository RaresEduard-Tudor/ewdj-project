package com.worldcup.validation;

import com.worldcup.dto.RegistrationDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PasswordsValidatorTest {

    private final Validator validator =
        Validation.buildDefaultValidatorFactory().getValidator();

    private RegistrationDto dto(String password, String confirmPassword) {
        RegistrationDto dto = new RegistrationDto();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword(password);
        dto.setConfirmPassword(confirmPassword);
        return dto;
    }

    @Test
    void matchingPasswords_shouldPass() {
        RegistrationDto reg = dto("password123", "password123");
        Set<ConstraintViolation<RegistrationDto>> violations = validator.validate(reg);
        assertTrue(violations.stream().noneMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidPasswords));
    }

    @Test
    void mismatchedPasswords_shouldFail() {
        RegistrationDto reg = dto("password123", "differentPassword");
        Set<ConstraintViolation<RegistrationDto>> violations = validator.validate(reg);
        assertTrue(violations.stream().anyMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidPasswords));
    }

    @Test
    void nullPassword_shouldFail() {
        RegistrationDto reg = dto(null, "password123");
        Set<ConstraintViolation<RegistrationDto>> violations = validator.validate(reg);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullConfirmPassword_shouldFail() {
        RegistrationDto reg = dto("password123", null);
        Set<ConstraintViolation<RegistrationDto>> violations = validator.validate(reg);
        assertFalse(violations.isEmpty());
    }
}
