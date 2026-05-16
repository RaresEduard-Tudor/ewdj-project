package com.worldcup.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SameGroupCountriesValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SameGroupCountries {
    String message() default "{validation.country.group}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
