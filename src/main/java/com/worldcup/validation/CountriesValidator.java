package com.worldcup.validation;

import com.worldcup.dto.MatchDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CountriesValidator implements ConstraintValidator<ValidCountries, MatchDto> {

    @Override
    public boolean isValid(MatchDto dto, ConstraintValidatorContext ctx) {
        if (dto.getCountryA() == null || dto.getCountryB() == null) return false;
        return !dto.getCountryA().equalsIgnoreCase(dto.getCountryB());
    }
}
