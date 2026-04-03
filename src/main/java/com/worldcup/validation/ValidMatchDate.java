package com.worldcup.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MatchDateValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMatchDate {
    String message() default "{validation.date.range}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
