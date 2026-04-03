package com.worldcup.validation;

import com.worldcup.dto.RegistrationDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsValidator implements ConstraintValidator<ValidPasswords, RegistrationDto> {

    @Override
    public boolean isValid(RegistrationDto dto, ConstraintValidatorContext ctx) {
        if (dto.getPassword() == null || dto.getConfirmPassword() == null) return false;
        return dto.getPassword().equals(dto.getConfirmPassword());
    }
}
