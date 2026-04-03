package com.worldcup.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MatchDateValidator implements ConstraintValidator<ValidMatchDate, LocalDateTime> {

    public static final LocalDate WC_START = LocalDate.of(2026, 6, 11);
    public static final LocalDate WC_END   = LocalDate.of(2026, 7, 19);

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext ctx) {
        if (value == null) return false;
        LocalDate date = value.toLocalDate();
        return !date.isBefore(WC_START) && !date.isAfter(WC_END);
    }
}
