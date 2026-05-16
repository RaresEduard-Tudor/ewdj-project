package com.worldcup.validation;

import com.worldcup.dto.MatchDto;
import com.worldcup.util.CountryRegistry;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SameGroupCountriesValidator implements ConstraintValidator<SameGroupCountries, MatchDto> {

    @Override
    public boolean isValid(MatchDto dto, ConstraintValidatorContext ctx) {
        if (dto == null) return true;
        String a = dto.getCountryA();
        String b = dto.getCountryB();
        if (a == null || b == null || a.isBlank() || b.isBlank()) return true;
        String groupA = CountryRegistry.groupOf(a);
        String groupB = CountryRegistry.groupOf(b);
        if (groupA == null || groupB == null) return true;
        return groupA.equals(groupB);
    }
}
