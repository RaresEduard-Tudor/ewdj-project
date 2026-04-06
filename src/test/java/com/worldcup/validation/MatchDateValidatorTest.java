package com.worldcup.validation;

import com.worldcup.dto.MatchDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class MatchDateValidatorTest {

    private final Validator validator =
        Validation.buildDefaultValidatorFactory().getValidator();

    private MatchDto validDto(LocalDateTime date) {
        MatchDto dto = new MatchDto();
        dto.setCountryA("Belgium");
        dto.setCountryB("France");
        dto.setStadiumCode("1234");
        dto.setChecksum(70); // 1234 % 97
        dto.setMatchDate(date);
        return dto;
    }

    @Test
    void dateWithinWC2026_shouldPass() {
        MatchDto dto = validDto(LocalDateTime.of(2026, 6, 15, 18, 0));
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().noneMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidMatchDate));
    }

    @Test
    void firstDayOfWC_shouldPass() {
        MatchDto dto = validDto(LocalDateTime.of(2026, 6, 11, 12, 0));
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().noneMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidMatchDate));
    }

    @Test
    void lastDayOfWC_shouldPass() {
        MatchDto dto = validDto(LocalDateTime.of(2026, 7, 19, 20, 0));
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().noneMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidMatchDate));
    }

    @Test
    void dateBeforeWC_shouldFail() {
        MatchDto dto = validDto(LocalDateTime.of(2026, 6, 10, 18, 0));
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidMatchDate));
    }

    @Test
    void dateAfterWC_shouldFail() {
        MatchDto dto = validDto(LocalDateTime.of(2026, 7, 20, 18, 0));
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidMatchDate));
    }

    @Test
    void dateInWrongYear_shouldFail() {
        MatchDto dto = validDto(LocalDateTime.of(2025, 6, 15, 18, 0));
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidMatchDate));
    }
}
