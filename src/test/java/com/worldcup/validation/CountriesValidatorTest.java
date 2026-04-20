package com.worldcup.validation;

import com.worldcup.dto.MatchDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CountriesValidatorTest {

    private final Validator validator =
        Validation.buildDefaultValidatorFactory().getValidator();

    private MatchDto validDto(String countryA, String countryB) {
        MatchDto dto = new MatchDto();
        dto.setCountryA(countryA);
        dto.setCountryB(countryB);
        dto.setStadiumCode("1234");
        dto.setChecksum(1234 % 97);
        dto.setMatchDate(LocalDateTime.of(2026, 6, 15, 18, 0));
        return dto;
    }

    @Test
    void differentCountries_shouldPass() {
        MatchDto dto = validDto("Brazil", "France");
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().noneMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidCountries));
    }

    @Test
    void sameCountries_shouldFail() {
        MatchDto dto = validDto("Brazil", "Brazil");
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidCountries));
    }

    @Test
    void sameCountriesCaseInsensitive_shouldFail() {
        MatchDto dto = validDto("brazil", "BRAZIL");
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidCountries));
    }

    @Test
    void nullCountryA_shouldFail() {
        MatchDto dto = validDto(null, "France");
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
