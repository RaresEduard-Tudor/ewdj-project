package com.worldcup.validation;

import com.worldcup.dto.MatchDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ChecksumValidatorTest {

    private final Validator validator =
        Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validChecksum_shouldPass() {
        MatchDto dto = new MatchDto();
        dto.setStadiumCode("1234");
        dto.setChecksum(1234 % 97); // = 67
        dto.setCountryA("Belgium");
        dto.setCountryB("France");
        dto.setMatchDate(LocalDateTime.of(2026, 6, 15, 18, 0));
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().noneMatch(v ->
            v.getConstraintDescriptor().getAnnotation() instanceof ValidChecksum));
    }

    @Test
    void invalidChecksum_shouldFail() {
        MatchDto dto = new MatchDto();
        dto.setStadiumCode("1234");
        dto.setChecksum(99); // wrong — should be 67
        dto.setCountryA("Belgium");
        dto.setCountryB("France");
        dto.setMatchDate(LocalDateTime.of(2026, 6, 15, 18, 0));
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void sameCountries_shouldFail() {
        MatchDto dto = new MatchDto();
        dto.setStadiumCode("1234");
        dto.setChecksum(67);
        dto.setCountryA("Belgium");
        dto.setCountryB("Belgium");
        dto.setMatchDate(LocalDateTime.of(2026, 6, 15, 18, 0));
        Set<ConstraintViolation<MatchDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
