package com.worldcup.validation;

import com.worldcup.dto.MatchDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SameGroupCountriesValidatorTest {

    private final Validator validator =
        Validation.buildDefaultValidatorFactory().getValidator();

    private MatchDto dto(String a, String b) {
        MatchDto dto = new MatchDto();
        dto.setCountryA(a);
        dto.setCountryB(b);
        dto.setStadiumCode("1234");
        dto.setChecksum(1234 % 97);
        dto.setMatchDate(LocalDateTime.of(2026, 6, 15, 18, 0));
        return dto;
    }

    @Test
    void sameGroup_shouldPass() {
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto("Belgium", "Egypt"));
        assertTrue(violations.stream().noneMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof SameGroupCountries));
    }

    @Test
    void differentGroups_shouldFail() {
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto("Brazil", "France"));
        assertTrue(violations.stream().anyMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof SameGroupCountries));
    }

    @Test
    void unknownCountry_shouldPass() {
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto("Atlantis", "Egypt"));
        assertTrue(violations.stream().noneMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof SameGroupCountries));
    }
}
