package com.worldcup.validation;

import com.worldcup.dto.RegistrationDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    private final Validator validator =
        Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validEmail_shouldPassValidation() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("user@example.com");
        dto.setUsername("testuser");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        Set<ConstraintViolation<RegistrationDto>> violations =
            validator.validateProperty(dto, "email");
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidEmail_shouldFailValidation() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("not-an-email");
        Set<ConstraintViolation<RegistrationDto>> violations =
            validator.validateProperty(dto, "email");
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullEmail_shouldFailValidation() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail(null);
        Set<ConstraintViolation<RegistrationDto>> violations =
            validator.validateProperty(dto, "email");
        assertFalse(violations.isEmpty());
    }
}
