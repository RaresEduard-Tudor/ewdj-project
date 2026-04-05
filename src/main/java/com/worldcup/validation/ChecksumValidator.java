package com.worldcup.validation;

import com.worldcup.dto.MatchDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChecksumValidator implements ConstraintValidator<ValidChecksum, MatchDto> {

    @Override
    public boolean isValid(MatchDto dto, ConstraintValidatorContext ctx) {
        if (dto.getStadiumCode() == null || dto.getChecksum() == null) return true;
        try {
            int code = Integer.parseInt(dto.getStadiumCode());
            return dto.getChecksum().equals(code % 97);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
